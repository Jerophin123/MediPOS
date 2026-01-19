import { Component, OnInit, OnDestroy, ViewChild, ElementRef, ChangeDetectorRef } from '@angular/core';
import { FormBuilder, FormGroup, FormArray, Validators } from '@angular/forms';
import { BillingService } from '../../core/services/billing.service';
import { InventoryService } from '../../core/services/inventory.service';
import { DialogService } from '../../core/services/dialog.service';
import { Medicine } from '../../core/models/medicine.model';
import { BillItemRequest, CreateBillRequest, BillResponse, PaymentMode, PaymentRequest } from '../../core/models/billing.model';
import { BrowserMultiFormatReader, NotFoundException, BarcodeFormat, DecodeHintType } from '@zxing/library';

interface BillItem {
  medicine?: Medicine;
  medicineId?: number;
  barcode?: string;
  quantity: number;
  unitPrice?: number;
  total?: number;
}

@Component({
  selector: 'app-billing',
  templateUrl: './billing.component.html',
  styleUrls: ['./billing.component.scss']
})
export class BillingComponent implements OnInit, OnDestroy {
  // Expose PaymentMode enum to template
  PaymentMode = PaymentMode;
  
  billForm: FormGroup;
  items: BillItem[] = [];
  isLoading = false;
  currentBill: BillResponse | null = null;
  searchBarcode = '';
  searchMedicine = '';
  
  // Camera scanning
  isScanning = false;
  hasCamera = false;
  private codeReader: BrowserMultiFormatReader | null = null;
  private stream: MediaStream | null = null;

  constructor(
    private fb: FormBuilder,
    private billingService: BillingService,
    private inventoryService: InventoryService,
    private dialogService: DialogService,
    private cdr: ChangeDetectorRef
  ) {
    this.billForm = this.fb.group({
      customerName: [''],
      customerPhone: [''],
      payments: this.fb.array([
        this.fb.group({
          mode: [PaymentMode.CASH, Validators.required],
          amount: [0, [Validators.required, Validators.min(0.01)]],
          cashProvided: [0, [Validators.min(0)]]
        })
      ])
    });
  }

  async ngOnInit(): Promise<void> {
    this.resetBill();
    // Check if camera is available
    await this.checkCameraAvailability();
  }

  ngOnDestroy(): void {
    this.stopScanning();
  }

  async checkCameraAvailability(): Promise<void> {
    try {
      const devices = await navigator.mediaDevices.enumerateDevices();
      this.hasCamera = devices.some(device => device.kind === 'videoinput');
    } catch (error) {
      console.error('Error checking camera availability:', error);
      this.hasCamera = false;
    }
  }

  async startScanning(): Promise<void> {
    if (!this.hasCamera) {
      this.dialogService.error('Camera not available. Please use manual entry.');
      return;
    }

    try {
      this.isScanning = true;
      // Force change detection to render the video element
      this.cdr.detectChanges();
      
      // Wait for Angular to render the element with retries
      let videoElement: HTMLVideoElement | null = null;
      let retries = 0;
      const maxRetries = 50; // 5 seconds total wait time
      
      while (!videoElement && retries < maxRetries) {
        await new Promise(resolve => setTimeout(resolve, 100));
        videoElement = document.getElementById('video-scanner') as HTMLVideoElement;
        if (!videoElement) {
          // Force another change detection cycle
          this.cdr.detectChanges();
        }
        retries++;
      }
      
      if (!videoElement) {
        console.error('Video element not found after retries');
        this.dialogService.error('Video element not found. Please refresh and try again.');
        this.isScanning = false;
        return;
      }
      
      // Configure barcode reader with multiple formats
      // Note: ZXing supports Code 128, Code 39, Code 93, ITF (2of5)
      // For Code 11, MSI, Pharmacode, Telepen - may need additional libraries
      const hints = new Map();
      const formats = [
        BarcodeFormat.CODE_128,        // Code 128 ✓
        BarcodeFormat.CODE_39,         // Code 39 ✓ (Full ASCII supported via hints)
        BarcodeFormat.CODE_93,         // Code 93 ✓
        BarcodeFormat.ITF,             // Interleaved 2 of 5 ✓
        BarcodeFormat.CODABAR,         // Similar to Code 11
        BarcodeFormat.EAN_13,          // EAN-13
        BarcodeFormat.EAN_8,           // EAN-8
        BarcodeFormat.UPC_A,           // UPC-A
        BarcodeFormat.UPC_E,           // UPC-E
        BarcodeFormat.DATA_MATRIX,     // Data Matrix
        BarcodeFormat.QR_CODE,         // QR Code
        BarcodeFormat.PDF_417,         // PDF417
        BarcodeFormat.AZTEC            // Aztec
      ];
      
      // Filter out any undefined formats
      const validFormats = formats.filter(f => f !== undefined);
      
      hints.set(DecodeHintType.POSSIBLE_FORMATS, validFormats);
      hints.set(DecodeHintType.TRY_HARDER, true);
      hints.set(DecodeHintType.ASSUME_GS1, false);
      hints.set(DecodeHintType.ASSUME_CODE_39_CHECK_DIGIT, false);
      
      this.codeReader = new BrowserMultiFormatReader(hints);
      
      // Get available video devices
      const videoInputDevices = await this.codeReader.listVideoInputDevices();
      
      if (videoInputDevices.length === 0) {
        this.dialogService.error('No camera devices found.');
        this.isScanning = false;
        return;
      }

      // Use the first available camera (usually the default)
      const selectedDeviceId = videoInputDevices[0].deviceId;

      // Get user media stream
      this.stream = await navigator.mediaDevices.getUserMedia({
        video: {
          deviceId: selectedDeviceId ? { exact: selectedDeviceId } : undefined,
          facingMode: 'environment' // Prefer back camera on mobile
        }
      });

      // Set the stream to the video element (we know it exists from the check above)
      if (!videoElement) {
        this.dialogService.error('Video element not found.');
        this.isScanning = false;
        return;
      }

      // Store reference to avoid null checks in callbacks
      const video = videoElement;
      
      video.srcObject = this.stream;
      video.setAttribute('playsinline', 'true');
      video.setAttribute('autoplay', 'true');
      video.setAttribute('muted', 'true');

      // Wait for video to be ready
      await new Promise((resolve) => {
        video.onloadedmetadata = () => {
          video.play().then(() => {
            resolve(true);
          }).catch((err) => {
            console.error('Error playing video:', err);
            resolve(false);
          });
        };
      });

      // Start scanning
      this.codeReader.decodeFromVideoDevice(
        selectedDeviceId,
        'video-scanner',
        (result, error) => {
          if (result) {
            const barcode = result.getText();
            this.handleScannedBarcode(barcode);
            this.stopScanning();
          }
          
          if (error && !(error instanceof NotFoundException)) {
            // NotFoundException is normal - it means no barcode found yet
            console.error('Scanning error:', error);
          }
        }
      );
    } catch (error: any) {
      console.error('Error starting camera:', error);
      if (error.name === 'NotAllowedError' || error.name === 'PermissionDeniedError') {
        this.dialogService.error('Camera permission denied. Please allow camera access.');
      } else if (error.name === 'NotFoundError' || error.name === 'DevicesNotFoundError') {
        this.dialogService.error('No camera found. Please check your device.');
      } else {
        this.dialogService.error('Failed to start camera: ' + (error.message || 'Unknown error'));
      }
      this.isScanning = false;
      this.stopScanning();
    }
  }

  stopScanning(): void {
    // Stop the video stream
    const videoElement = document.getElementById('video-scanner') as HTMLVideoElement;
    if (videoElement) {
      videoElement.srcObject = null;
      videoElement.pause();
    }
    
    if (this.codeReader) {
      this.codeReader.reset();
      this.codeReader = null;
    }
    
    if (this.stream) {
      this.stream.getTracks().forEach(track => track.stop());
      this.stream = null;
    }
    
    this.isScanning = false;
  }

  handleScannedBarcode(barcode: string): void {
    if (!barcode || !barcode.trim()) {
      return;
    }

    // Update the search input
    this.searchBarcode = barcode.trim();
    
    // Process the barcode
    this.processBarcode(barcode.trim());
  }

  processBarcode(barcode: string): void {
    this.isLoading = true;
    this.inventoryService.findMedicineByBarcode(barcode).subscribe({
      next: (medicine) => {
        this.addItemByMedicine(medicine, 1, barcode);
        this.searchBarcode = '';
        this.isLoading = false;
        this.dialogService.success(`Scanned: ${medicine.name}`);
      },
      error: (error: any) => {
        this.dialogService.error(error.message || 'Medicine not found with this barcode');
        this.isLoading = false;
      }
    });
  }

  get paymentsFormArray(): FormArray {
    return this.billForm.get('payments') as FormArray;
  }

  get subtotal(): number {
    // Calculate subtotal without GST
    return this.items.reduce((sum, item) => {
      const itemSubtotal = (item.unitPrice || 0) * item.quantity;
      return sum + itemSubtotal;
    }, 0);
  }

  get totalGst(): number {
    // Calculate GST for each item based on medicine's GST percentage from database
    const total = this.items.reduce((sum, item) => {
      // Ensure we have both medicine with gstPercentage and unitPrice
      if (!item.medicine || !item.medicine.gstPercentage || !item.unitPrice) {
        // If medicine is missing but we have medicineId, try to get it (async, so skip for now)
        // This will be handled by updateItemTotal when price is fetched
        return sum;
      }
      
      // Get GST percentage from medicine (from database)
      const gstPercentage = item.medicine.gstPercentage;
      if (gstPercentage <= 0 || gstPercentage > 100) {
        // Invalid GST percentage, skip
        return sum;
      }
      
      // Calculate item subtotal (price × quantity)
      const itemSubtotal = item.unitPrice * item.quantity;
      
      // Calculate GST amount: (subtotal × gstPercentage) / 100
      const gstAmount = (itemSubtotal * gstPercentage) / 100;
      
      return sum + gstAmount;
    }, 0);
    
    // Round to 2 decimal places to match backend precision
    return Math.round(total * 100) / 100;
  }

  get totalAmount(): number {
    return this.subtotal + this.totalGst;
  }

  getCashProvided(index: number): number {
    const payment = this.paymentsFormArray.at(index);
    if (payment.get('mode')?.value === PaymentMode.CASH) {
      return payment.get('cashProvided')?.value || 0;
    }
    return 0;
  }

  getCashChange(index: number): number {
    const payment = this.paymentsFormArray.at(index);
    if (payment.get('mode')?.value === PaymentMode.CASH) {
      const cashProvided = payment.get('cashProvided')?.value || 0;
      const amount = payment.get('amount')?.value || 0;
      return Math.max(0, cashProvided - amount);
    }
    return 0;
  }

  onPaymentModeChange(index: number): void {
    const payment = this.paymentsFormArray.at(index);
    const mode = payment.get('mode')?.value;
    if (mode === PaymentMode.CASH) {
      payment.get('cashProvided')?.setValue(0);
    }
  }

  resetBill(): void {
    this.items = [];
    this.currentBill = null;
    this.searchBarcode = '';
    this.searchMedicine = '';
    this.billForm.reset({
      customerName: '',
      customerPhone: '',
      payments: [
        { mode: PaymentMode.CASH, amount: 0, cashProvided: 0 }
      ]
    });
    this.paymentsFormArray.clear();
    this.addPayment();
  }

  addPayment(): void {
    const paymentForm = this.fb.group({
      mode: [PaymentMode.CASH, Validators.required],
      amount: [0, [Validators.required, Validators.min(0.01)]],
      cashProvided: [0, [Validators.min(0)]]
    });
    this.paymentsFormArray.push(paymentForm);
  }

  removePayment(index: number): void {
    if (this.paymentsFormArray.length > 1) {
      this.paymentsFormArray.removeAt(index);
    }
  }

  onBarcodeScan(): void {
    if (!this.searchBarcode.trim()) {
      return;
    }

    this.processBarcode(this.searchBarcode.trim());
  }

  onMedicineSearch(): void {
    if (!this.searchMedicine.trim() || this.searchMedicine.trim().length < 2) {
      return;
    }

    this.isLoading = true;
    this.inventoryService.searchMedicines(this.searchMedicine.trim()).subscribe({
      next: (medicines) => {
        if (medicines && medicines.length > 0) {
          // Add first result, or show selection dialog
          this.addItemByMedicine(medicines[0], 1);
          this.searchMedicine = '';
        } else {
          this.dialogService.warning('No medicines found');
        }
        this.isLoading = false;
      },
      error: (error: any) => {
        this.dialogService.error(error.message || 'Error searching medicines');
        this.isLoading = false;
      }
    });
  }

  addItemByMedicine(medicine: Medicine, quantity: number, barcode?: string): void {
    // Check if medicine already in items
    const existingIndex = this.items.findIndex(item => item.medicineId === medicine.id);
    
    if (existingIndex >= 0) {
      // Update quantity
      this.items[existingIndex].quantity += quantity;
      if (barcode && !this.items[existingIndex].barcode) {
        this.items[existingIndex].barcode = barcode;
      }
      this.updateItemTotal(this.items[existingIndex]);
    } else {
      // Add new item
      const item: BillItem = {
        medicine,
        medicineId: medicine.id,
        barcode,
        quantity,
        unitPrice: 0, // Will be fetched from batch
        total: 0
      };
      this.items.push(item);
      
      // Fetch price from available batches
      this.fetchItemPrice(item);
    }
  }

  fetchItemPrice(item: BillItem): void {
    if (!item.medicineId) return;
    
    // Ensure medicine object is preserved - if missing, refetch it
    if (!item.medicine || !item.medicine.gstPercentage) {
      this.inventoryService.getMedicineById(item.medicineId).subscribe({
        next: (medicine) => {
          item.medicine = medicine;
          // Now fetch price
          this.fetchPriceFromBatches(item);
        },
        error: (error) => {
          console.error('Error fetching medicine:', error);
        }
      });
    } else {
      // Medicine object is present, just fetch price
      this.fetchPriceFromBatches(item);
    }
  }

  private fetchPriceFromBatches(item: BillItem): void {
    if (!item.medicineId) return;
    
    this.inventoryService.getBatchesByMedicine(item.medicineId).subscribe({
      next: (batches) => {
        // Get the first non-expired batch with available stock
        const availableBatch = batches.find(b => !b.expired && b.quantityAvailable > 0);
        if (availableBatch && availableBatch.sellingPrice) {
          item.unitPrice = availableBatch.sellingPrice;
          this.updateItemTotal(item);
        }
      },
      error: (error) => {
        // Silently fail - price will be calculated by backend
        console.error('Error fetching price:', error);
      }
    });
  }

  updateItemTotal(item: BillItem): void {
    if (item.unitPrice) {
      // Ensure medicine object is available with gstPercentage
      if (!item.medicine || !item.medicine.gstPercentage) {
        // Refetch medicine if missing
        if (item.medicineId) {
          this.inventoryService.getMedicineById(item.medicineId).subscribe({
            next: (medicine) => {
              item.medicine = medicine;
              this.calculateItemTotal(item);
            },
            error: (error) => {
              console.error('Error fetching medicine for GST calculation:', error);
              // Calculate without GST if medicine fetch fails
              this.calculateItemTotal(item);
            }
          });
          return;
        }
      }
      this.calculateItemTotal(item);
    }
  }

  private calculateItemTotal(item: BillItem): void {
    if (!item.unitPrice) return;
    
    // Calculate item total including GST (for display purposes)
    // Backend will recalculate, but this gives user immediate feedback
    const itemSubtotal = item.unitPrice * item.quantity;
    const gstPercentage = item.medicine?.gstPercentage || 0;
    const gstAmount = gstPercentage > 0 
      ? (itemSubtotal * gstPercentage) / 100 
      : 0;
    item.total = itemSubtotal + gstAmount;
    
    // Debug logging (can be removed in production)
    if (gstPercentage > 0) {
      console.log(`Item: ${item.medicine?.name}, Subtotal: ${itemSubtotal}, GST%: ${gstPercentage}, GST Amount: ${gstAmount}, Total: ${item.total}`);
    }
  }

  updateQuantity(index: number, quantity: number): void {
    if (quantity > 0) {
      this.items[index].quantity = quantity;
      this.updateItemTotal(this.items[index]);
      // Force change detection for GST recalculation
      this.items = [...this.items];
    }
  }

  removeItem(index: number): void {
    this.items.splice(index, 1);
  }

  createBill(): void {
    if (this.items.length === 0) {
      this.dialogService.warning('Please add at least one item to the bill');
      return;
    }

    // Validate that all items have required fields
    const invalidItems = this.items.filter(item => !item.medicineId && !item.barcode);
    if (invalidItems.length > 0) {
      this.dialogService.warning('Some items are missing medicine information. Please remove and re-add them.');
      return;
    }

    this.isLoading = true;

    // Map items to bill items (exclude internal fields)
    const billItems: BillItemRequest[] = this.items.map(item => {
      // Ensure at least one identifier is present
      if (!item.medicineId && !item.barcode) {
        throw new Error(`Item ${item.medicine?.name || 'Unknown'} is missing medicine ID or barcode`);
      }
      return {
        medicineId: item.medicineId || undefined,
        barcode: item.barcode || undefined,
        quantity: item.quantity || 1 // Ensure quantity is at least 1
      };
    });

    // Validate items
    if (billItems.length === 0) {
      this.dialogService.warning('Please add at least one item to the bill');
      this.isLoading = false;
      return;
    }

    // Map payments to payment requests (exclude cashProvided which is frontend-only)
    const payments: PaymentRequest[] = this.paymentsFormArray.value
      .filter((p: any) => {
        const amount = Number(p.amount);
        return !isNaN(amount) && amount > 0;
      })
      .map((p: any) => {
        const amount = Number(p.amount);
        if (isNaN(amount) || amount <= 0) {
          throw new Error(`Invalid payment amount: ${p.amount}`);
        }
        return {
          mode: p.mode,
          amount: amount,
          paymentReference: p.paymentReference || undefined
        };
      });

    // Validate payments
    if (payments.length === 0) {
      this.dialogService.warning('Please add at least one payment with amount greater than 0');
      this.isLoading = false;
      return;
    }

    const request: CreateBillRequest = {
      items: billItems,
      customerName: this.billForm.get('customerName')?.value?.trim() || undefined,
      customerPhone: this.billForm.get('customerPhone')?.value?.trim() || undefined,
      payments: payments
    };

    // Debug: Log the request being sent
    console.log('Creating bill with request:', JSON.stringify(request, null, 2));

    this.billingService.createBill(request).subscribe({
      next: async (bill) => {
        this.currentBill = bill;
        const message = `Bill created successfully: ${bill.billNumber}\n\nWould you like to download the PDF receipt?`;
        const confirmed = await this.dialogService.confirm(message, 'Bill Created');
        if (confirmed) {
          this.downloadBillPdf(bill.id);
        }
        this.resetBill();
        this.isLoading = false;
      },
      error: (error: any) => {
        console.error('Error creating bill:', error);
        // Extract error message from response
        let errorMessage = 'Error creating bill';
        
        if (error.error) {
          // Check for validation field errors
          if (error.error.fieldErrors && Array.isArray(error.error.fieldErrors) && error.error.fieldErrors.length > 0) {
            const fieldErrors = error.error.fieldErrors
              .map((fe: any) => `${fe.field}: ${fe.message}`)
              .join('\n');
            errorMessage = `Validation failed:\n${fieldErrors}`;
          } else if (error.error.message) {
            errorMessage = error.error.message;
          } else if (error.error.error) {
            errorMessage = error.error.error;
          }
        } else if (error.message) {
          errorMessage = error.message;
        }
        
        this.dialogService.error(errorMessage);
        this.isLoading = false;
      }
    });
  }

  downloadBillPdf(billId: number): void {
    this.isLoading = true;
    this.billingService.downloadBillPdf(billId).subscribe({
      next: (blob: Blob) => {
        // Create a download link
        const url = window.URL.createObjectURL(blob);
        const link = document.createElement('a');
        link.href = url;
        link.download = `Bill_${this.currentBill?.billNumber || billId}.pdf`;
        document.body.appendChild(link);
        link.click();
        document.body.removeChild(link);
        window.URL.revokeObjectURL(url);
        this.isLoading = false;
      },
      error: (error: any) => {
        console.error('Error downloading PDF:', error);
        this.dialogService.error('Error downloading PDF: ' + (error.message || 'Unknown error'));
        this.isLoading = false;
      }
    });
  }
}
