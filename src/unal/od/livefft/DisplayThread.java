/*    */ package unal.od.livefft;
/*    */ 
/*    */ import java.util.logging.Level;
/*    */ import java.util.logging.Logger;
/*    */ 
/*    */ 
/*    */ 
/*    */ 
/*    */ 
/*    */ 
/*    */ 
/*    */ 
/*    */ 
/*    */ 
/*    */ public class DisplayThread
/*    */   extends Thread
/*    */ {
/*    */   private final LiveFFT_ live;
/*    */   volatile boolean stop = false;
/*    */   volatile boolean running = false;
/*    */   
/*    */   public DisplayThread(LiveFFT_ frame) {
/* 23 */     this.live = frame;
/*    */   }
/*    */ 
/*    */   
/*    */   public void run() {
/* 28 */     this.stop = false;
/* 29 */     this.running = true;
/* 30 */     int i = 0;
/*    */     
/* 32 */     while (!this.stop) {
/* 33 */       long time = System.nanoTime();
/* 34 */       boolean ret = this.live.updateImage();
/*    */       
/* 36 */       if (!ret) {
/* 37 */         this.running = false;
/*    */         
/*    */         return;
/*    */       } 
/*    */       try {
/* 42 */         sleep(10L);
/* 43 */       } catch (InterruptedException ex) {
/* 44 */         Logger.getLogger(DisplayThread.class.getName()).log(Level.SEVERE, (String)null, ex);
/*    */       } 
/*    */       
/* 47 */       time = System.nanoTime() - time;
/* 48 */       double fps = 1.0D / time * 1.0E-9D;
/*    */ 
/*    */       
/* 51 */       i++;
/* 52 */       if (i % 10 == 0) {
/* 53 */         this.live.getImageWindow().setFPS(fps);
/* 54 */         i = 0;
/*    */       } 
/*    */     } 
/*    */ 
/*    */     
/* 59 */     this.running = false;
/*    */   }
/*    */   
/*    */   public void stopDisplay() {
/* 63 */     this.stop = true;
/*    */   }
/*    */   
/*    */   public boolean isRunning() {
/* 67 */     return this.running;
/*    */   }
/*    */ }


/* Location:              C:\Users\Carlos Buitrago\Documents\00 - Optodigital\Software\ImageJ-Plugins\LiveFFT\LiveFFT_1.0.jar\\unal\od\livefft\DisplayThread.class
 * Java compiler version: 6 (50.0)
 * JD-Core Version:       1.1.3
 */