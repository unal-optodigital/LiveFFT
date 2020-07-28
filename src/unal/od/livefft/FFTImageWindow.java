/*     */ package unal.od.livefft;
/*     */ 
/*     */ import ij.ImageListener;
/*     */ import ij.ImagePlus;
/*     */ import ij.gui.ImageWindow;
/*     */ import ij.process.ImageProcessor;
/*     */ import java.awt.Button;
/*     */ import java.awt.Checkbox;
/*     */ import java.awt.Color;
/*     */ import java.awt.Font;
/*     */ import java.awt.Panel;
/*     */ import java.awt.event.ActionEvent;
/*     */ import java.awt.event.ActionListener;
/*     */ import java.awt.event.ItemEvent;
/*     */ import java.awt.event.ItemListener;
/*     */ import java.awt.event.WindowEvent;
/*     */ import java.util.Locale;
/*     */ import java.util.prefs.Preferences;
/*     */ import javax.swing.JTextField;
/*     */ 
/*     */ 
/*     */ 
/*     */ 
/*     */ 
/*     */ 
/*     */ 
/*     */ 
/*     */ 
/*     */ 
/*     */ 
/*     */ public class FFTImageWindow
/*     */   extends ImageWindow
/*     */   implements ImageListener
/*     */ {
/*     */   private static final String TITLE_PREFIX = "Live FFT of ";
/*     */   private final LiveFFT_ liveFFT;
/*     */   private final Preferences prefs;
/*     */   private Button snapBtn;
/*     */   private Button liveBtn;
/*     */   private Checkbox logScaleChk;
/*     */   private JTextField fpsField;
/*     */   private Panel panel;
/*     */   private boolean log;
/*     */   private boolean live;
/*     */   private boolean imageClosed;
/*     */   private final int ID;
/*     */   
/*     */   public FFTImageWindow(ImagePlus imp, String name, LiveFFT_ liveFFT) {
/*  49 */     super(imp);
/*  50 */     imp.setTitle("Live FFT of " + name);
/*  51 */     this.liveFFT = liveFFT;
/*     */     
/*  53 */     this.prefs = Preferences.userNodeForPackage(getClass());
/*     */     
/*  55 */     this.log = this.prefs.getBoolean("LOG", true);
/*  56 */     liveFFT.setLog(this.log);
/*  57 */     this.live = true;
/*  58 */     this.imageClosed = false;
/*  59 */     this.ID = liveFFT.getID();
/*     */     
/*  61 */     initialize();
/*     */   }
/*     */   
/*     */   private void initialize() {
/*  65 */     this.panel = new Panel();
/*     */     
/*  67 */     this.snapBtn = new Button("Snap");
/*  68 */     this.snapBtn.addActionListener(new ActionListener()
/*     */         {
/*     */           public void actionPerformed(ActionEvent e) {
/*  71 */             ImagePlus imp = FFTImageWindow.this.getImagePlus().duplicate();
/*  72 */             imp.setTitle("Snapped Spectrum");
/*  73 */             imp.show();
/*     */           }
/*     */         });
/*  76 */     this.panel.add(this.snapBtn);
/*     */     
/*  78 */     this.liveBtn = new Button("Live");
/*  79 */     this.liveBtn.addActionListener(new ActionListener()
/*     */         {
/*     */           public void actionPerformed(ActionEvent e)
/*     */           {
/*  83 */             if (FFTImageWindow.this.imageClosed) {
/*     */               return;
/*     */             }
/*     */             
/*  87 */             if (FFTImageWindow.this.liveBtn.getForeground() == Color.RED) {
/*  88 */               FFTImageWindow.this.liveFFT.stopLive();
/*  89 */               Font font = FFTImageWindow.this.liveBtn.getFont();
/*  90 */               FFTImageWindow.this.liveBtn.setFont(new Font(font.getName(), 0, font.getSize()));
/*  91 */               FFTImageWindow.this.liveBtn.setForeground(Color.BLACK);
/*     */               
/*  93 */               FFTImageWindow.this.live = false;
/*  94 */               FFTImageWindow.this.logScaleChk.setEnabled(false);
/*     */             } else {
/*  96 */               FFTImageWindow.this.liveFFT.startLive();
/*  97 */               Font font = FFTImageWindow.this.liveBtn.getFont();
/*  98 */               FFTImageWindow.this.liveBtn.setFont(new Font(font.getName(), 1, font.getSize()));
/*  99 */               FFTImageWindow.this.liveBtn.setForeground(Color.RED);
/*     */               
/* 101 */               FFTImageWindow.this.live = true;
/* 102 */               FFTImageWindow.this.logScaleChk.setEnabled(true);
/*     */             } 
/*     */           }
/*     */         });
/* 106 */     this.panel.add(this.liveBtn);
/*     */     
/* 108 */     this.logScaleChk = new Checkbox("Log. Scale");
/* 109 */     this.logScaleChk.addItemListener(new ItemListener()
/*     */         {
/*     */           public void itemStateChanged(ItemEvent e)
/*     */           {
/* 113 */             FFTImageWindow.this.log = FFTImageWindow.this.logScaleChk.getState();
/* 114 */             FFTImageWindow.this.liveFFT.setLog(FFTImageWindow.this.log);
/*     */           }
/*     */         });
/* 117 */     this.logScaleChk.setEnabled(true);
/* 118 */     this.logScaleChk.setState(this.log);
/* 119 */     this.panel.add(this.logScaleChk);
/*     */     
/* 121 */     this.fpsField = new JTextField(String.format(Locale.US, "%.3f fps", new Object[] {
/* 122 */             Double.valueOf(0.0D) }), 8);
/* 123 */     this.fpsField.setHorizontalAlignment(0);
/* 124 */     this.fpsField.setEditable(false);
/* 125 */     this.panel.add(this.fpsField);
/*     */     
/* 127 */     add(this.panel);
/* 128 */     pack();
/*     */     
/* 130 */     ImagePlus.addImageListener(this);
/*     */     
/* 132 */     Font font = this.liveBtn.getFont();
/* 133 */     this.liveBtn.setFont(new Font(font.getName(), 1, font.getSize()));
/* 134 */     this.liveBtn.setForeground(Color.RED);
/*     */   }
/*     */   
/*     */   public void setFPS(double fps) {
/* 138 */     this.fpsField.setText(String.format(Locale.US, "%.3f", new Object[] { Double.valueOf(fps) }) + " fps");
/*     */   }
/*     */   
/*     */   public void newImage(Object o) {
/* 142 */     ImagePlus imp1 = getImagePlus();
/*     */     
/* 144 */     if (imp1 != null) {
/*     */       
/* 146 */       ImageProcessor ip = imp1.getProcessor();
/*     */       
/* 148 */       if (ip != null) {
/* 149 */         ip.setPixels(o);
/*     */       }
/*     */       
/* 152 */       imp1.updateAndDraw();
/*     */     } 
/*     */   }
/*     */ 
/*     */   
/*     */   public void imageClosed(ImagePlus imp) {
/* 158 */     if (imp.getID() == this.ID) {
/* 159 */       if (this.live) {
/* 160 */         this.liveFFT.stopLive();
/*     */         
/* 162 */         Font font = this.liveBtn.getFont();
/* 163 */         this.liveBtn.setFont(new Font(font.getName(), 0, font.getSize()));
/* 164 */         this.liveBtn.setForeground(Color.BLACK);
/*     */         
/* 166 */         this.logScaleChk.setEnabled(false);
/*     */       } 
/*     */       
/* 169 */       this.imageClosed = true;
/*     */     } 
/*     */   }
/*     */ 
/*     */ 
/*     */ 
/*     */   
/*     */   public void imageOpened(ImagePlus imp) {}
/*     */ 
/*     */ 
/*     */   
/*     */   public void imageUpdated(ImagePlus imp) {}
/*     */ 
/*     */ 
/*     */   
/*     */   public void windowClosing(WindowEvent e) {
/* 185 */     this.liveFFT.stopLive();
/* 186 */     this.prefs.putBoolean("LOG", this.logScaleChk.getState());
/*     */     
/* 188 */     ImagePlus.removeImageListener(this);
/*     */     
/* 190 */     super.windowClosing(e);
/*     */   }
/*     */ }


/* Location:              C:\Users\Carlos Buitrago\Documents\00 - Optodigital\Software\ImageJ-Plugins\LiveFFT\LiveFFT_1.0.jar\\unal\od\livefft\FFTImageWindow.class
 * Java compiler version: 6 (50.0)
 * JD-Core Version:       1.1.3
 */