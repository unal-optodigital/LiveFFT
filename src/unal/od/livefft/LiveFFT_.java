/*     */ package unal.od.livefft;
/*     */ 
/*     */ import ij.IJ;
/*     */ import ij.ImageJ;
/*     */ import ij.ImagePlus;
/*     */ import ij.plugin.filter.PlugInFilter;
/*     */ import ij.process.ByteProcessor;
/*     */ import ij.process.ImageProcessor;
/*     */ import java.io.File;
/*     */ import java.util.logging.Level;
/*     */ import java.util.logging.Logger;
/*     */ import javax.swing.UIManager;
/*     */ import javax.swing.UnsupportedLookAndFeelException;
/*     */ import org.jtransforms.fft.FloatFFT_2D;
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
/*     */ 
/*     */ 
/*     */ 
/*     */ 
/*     */ 
/*     */ public class LiveFFT_
/*     */   implements PlugInFilter
/*     */ {
/*     */   private ImagePlus imp;
/*     */   private ImageProcessor ip;
/*     */   private FloatFFT_2D fft;
/*     */   private DisplayThread dspThread;
/*     */   private FFTImageWindow imgWnd;
/*     */   private float[] field;
/*     */   private float[] spectrum;
/*     */   private byte[] scaledSpectrum;
/*     */   private int bitDepth;
/*     */   private int M;
/*     */   private int N;
/*     */   private boolean live;
/*     */   private boolean log;
/*     */   private boolean mod;
/*     */   private String imageTitle;
/*     */   
/*     */   public int setup(String string, ImagePlus imp) {
/*  51 */     this.bitDepth = imp.getBitDepth();
/*     */     
/*  53 */     this.M = imp.getWidth();
/*  54 */     this.N = imp.getHeight();
/*     */     
/*  56 */     this.imageTitle = imp.getTitle();
/*     */     
/*  58 */     this.imp = imp;
/*  59 */     this.fft = new FloatFFT_2D(this.M, this.N);
/*  60 */     this.dspThread = null;
/*  61 */     this.imgWnd = null;
/*     */     
/*  63 */     this.field = new float[this.M * 2 * this.N];
/*  64 */     this.spectrum = new float[this.M * this.N];
/*  65 */     this.scaledSpectrum = new byte[this.M * this.N];
/*     */     
/*  67 */     setLF();
/*     */     
/*  69 */     return 137;
/*     */   }
/*     */ 
/*     */   
/*     */   public void run(ImageProcessor ip) {
/*  74 */     this.ip = ip;
/*     */     
/*  76 */     startLive();
/*     */   }
/*     */   
/*     */   public FFTImageWindow getImageWindow() {
/*  80 */     return this.imgWnd;
/*     */   }
/*     */   
/*     */   public int getID() {
/*  84 */     return this.imp.getID();
/*     */   }
/*     */   
/*     */   public void setModulus(boolean mod) {
/*  88 */     this.mod = mod;
/*     */   }
/*     */   
/*     */   public void setLog(boolean log) {
/*  92 */     this.log = log;
/*     */   }
/*     */ 
/*     */   
/*     */   public boolean updateImage() {
/*  97 */     switch (this.bitDepth) {
/*     */       case 8:
/*  99 */         complexArray((byte[])this.ip.getPixels(), this.field);
/*     */         break;
/*     */       case 16:
/* 102 */         complexArray((short[])this.ip.getPixels(), this.field);
/*     */         break;
/*     */       case 32:
/* 105 */         complexArray((float[])this.ip.getPixels(), this.field);
/*     */         break;
/*     */     } 
/*     */     
/* 109 */     this.fft.complexForward(this.field);
/*     */     
/* 111 */     if (this.mod) {
/* 112 */       modulus(this.field, this.spectrum);
/*     */     } else {
/* 114 */       modulusSq(this.field, this.spectrum);
/*     */     } 
/*     */     
/* 117 */     shift(this.spectrum);
/* 118 */     if (this.log) {
/* 119 */       log10(this.spectrum);
/*     */     }
/* 121 */     scale(this.spectrum, this.scaledSpectrum);
/*     */     
/* 123 */     if (this.imgWnd == null || this.imgWnd.isClosed()) {
/*     */       
/* 125 */       ByteProcessor byteProcessor = new ByteProcessor(this.M, this.N, this.scaledSpectrum);
/*     */       
/* 127 */       ImagePlus impTmp = new ImagePlus("", (ImageProcessor)byteProcessor);
/* 128 */       this.imgWnd = new FFTImageWindow(impTmp, this.imageTitle, this);
/*     */     } 
/*     */     
/* 131 */     if (this.imgWnd != null) {
/* 132 */       this.imgWnd.newImage(this.scaledSpectrum);
/*     */     }
/*     */     
/* 135 */     return true;
/*     */   }
/*     */   
/*     */   public void startLive() {
/* 139 */     if (this.dspThread == null) {
/* 140 */       this.dspThread = new DisplayThread(this);
/*     */     }
/*     */     
/* 143 */     this.dspThread.start();
/*     */   }
/*     */   
/*     */   public void stopLive() {
/* 147 */     this.dspThread.stopDisplay();
/*     */     try {
/* 149 */       this.dspThread.join();
/* 150 */     } catch (InterruptedException ex) {
/* 151 */       Logger.getLogger(LiveFFT_.class.getName()).log(Level.SEVERE, (String)null, ex);
/*     */     } 
/* 153 */     this.dspThread = null;
/*     */   }
/*     */   
/*     */   private void setLF() {
/*     */     try {
/* 158 */       UIManager.setLookAndFeel(UIManager.getSystemLookAndFeelClassName());
/* 159 */     } catch (ClassNotFoundException e) {
/*     */     
/* 161 */     } catch (InstantiationException e) {
/*     */     
/* 163 */     } catch (IllegalAccessException e) {
/*     */     
/* 165 */     } catch (UnsupportedLookAndFeelException e) {}
/*     */   }
/*     */ 
/*     */ 
/*     */   
/*     */   private void complexArray(byte[] real, float[] complex) {
/* 171 */     for (int i = 0; i < this.M; i++) {
/* 172 */       for (int j = 0; j < this.N; j++) {
/* 173 */         int pos = i * 2 * this.N + 2 * j;
/* 174 */         complex[pos] = real[j * this.M + i];
/* 175 */         complex[pos + 1] = 0.0F;
/*     */       } 
/*     */     } 
/*     */   }
/*     */   
/*     */   private void complexArray(short[] real, float[] complex) {
/* 181 */     for (int i = 0; i < this.M; i++) {
/* 182 */       for (int j = 0; j < this.N; j++) {
/* 183 */         int pos = i * 2 * this.N + 2 * j;
/* 184 */         complex[pos] = real[j * this.M + i];
/* 185 */         complex[pos + 1] = 0.0F;
/*     */       } 
/*     */     } 
/*     */   }
/*     */   
/*     */   private void complexArray(float[] real, float[] complex) {
/* 191 */     for (int i = 0; i < this.M; i++) {
/* 192 */       for (int j = 0; j < this.N; j++) {
/* 193 */         int pos = i * 2 * this.N + 2 * j;
/* 194 */         complex[pos] = real[j * this.M + i];
/* 195 */         complex[pos + 1] = 0.0F;
/*     */       } 
/*     */     } 
/*     */   }
/*     */   
/*     */   private void modulus(float[] field, float[] spectrum) {
/* 201 */     for (int i = 0; i < this.M; i++) {
/* 202 */       for (int j = 0; j < this.N; j++) {
/* 203 */         int pos = i * 2 * this.N + 2 * j;
/* 204 */         float real = field[pos];
/* 205 */         float imag = field[pos + 1];
/*     */         
/* 207 */         spectrum[j * this.M + i] = (float)Math.sqrt((real * real + imag * imag));
/*     */       } 
/*     */     } 
/*     */   }
/*     */   
/*     */   private void modulusSq(float[] field, float[] spectrum) {
/* 213 */     for (int i = 0; i < this.M; i++) {
/* 214 */       for (int j = 0; j < this.N; j++) {
/* 215 */         int pos = i * 2 * this.N + 2 * j;
/* 216 */         float real = field[pos];
/* 217 */         float imag = field[pos + 1];
/* 218 */         spectrum[j * this.M + i] = real * real + imag * imag;
/*     */       } 
/*     */     } 
/*     */   }
/*     */ 
/*     */   
/*     */   private void shift(float[] spectrum) {
/* 225 */     int M2 = this.M / 2;
/* 226 */     int N2 = this.N / 2;
/*     */ 
/*     */ 
/*     */     
/* 230 */     for (int i = 0; i < M2; i++) {
/* 231 */       for (int j = 0; j < N2; j++) {
/* 232 */         float tmp1 = spectrum[j * this.M + i];
/* 233 */         spectrum[j * this.M + i] = spectrum[(j + N2) * this.M + i + M2];
/* 234 */         spectrum[(j + N2) * this.M + i + M2] = tmp1;
/*     */         
/* 236 */         tmp1 = spectrum[j * this.M + i + M2];
/* 237 */         spectrum[j * this.M + i + M2] = spectrum[(j + N2) * this.M + i];
/* 238 */         spectrum[(j + N2) * this.M + i] = tmp1;
/*     */       } 
/*     */     } 
/*     */   }
/*     */   
/*     */   private void log10(float[] spectrum) {
/* 244 */     for (int i = 0; i < spectrum.length; i++) {
/* 245 */       spectrum[i] = (float)Math.log10(spectrum[i]);
/*     */     }
/*     */   }
/*     */   
/*     */   private void scale(float[] spectrum, byte[] scaledSpectrum) {
/* 250 */     float max = Float.MIN_VALUE;
/* 251 */     float min = Float.MAX_VALUE;
/*     */     
/* 253 */     for (int i = 0; i < spectrum.length; i++) {
/* 254 */       if (spectrum[i] < min) {
/* 255 */         min = spectrum[i];
/*     */       }
/*     */       
/* 258 */       if (spectrum[i] > max) {
/* 259 */         max = spectrum[i];
/*     */       }
/*     */     } 
/*     */     
/* 263 */     float delta = max - min;
/*     */     
/* 265 */     for (int j = 0; j < spectrum.length; j++) {
/* 266 */       float val = spectrum[j] - min;
/* 267 */       val /= delta;
/* 268 */       val *= 255.0F;
/*     */       
/* 270 */       scaledSpectrum[j] = (byte)(int)val;
/*     */     } 
/*     */   }
/*     */ 
/*     */   
/*     */   public static void main(String[] args) {
/* 276 */     Class<?> clazz = LiveFFT_.class;
/* 277 */     String url = clazz.getResource("/" + clazz.getName().replace('.', '/') + ".class").toString();
/* 278 */     String pluginsDir = url.substring(5, url.length() - clazz.getName().length() - 6);
/* 279 */     System.setProperty("plugins.dir", pluginsDir);
/*     */ 
/*     */     
/* 282 */     ImageJ ij = new ImageJ();
/*     */ 
/*     */     
/* 285 */     File f = new File("holo2.bmp");
/* 286 */     ImagePlus image = IJ.openImage(f.getAbsolutePath());
/* 287 */     image.show();
/*     */ 
/*     */     
/* 290 */     IJ.runPlugIn(clazz.getName(), "");
/*     */   }
/*     */ }


/* Location:              C:\Users\Carlos Buitrago\Documents\00 - Optodigital\Software\ImageJ-Plugins\LiveFFT\LiveFFT_1.0.jar\\unal\od\livefft\LiveFFT_.class
 * Java compiler version: 6 (50.0)
 * JD-Core Version:       1.1.3
 */