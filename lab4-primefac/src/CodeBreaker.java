

import java.awt.Component;
import java.math.BigInteger;
import java.util.concurrent.Executor;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.ThreadFactory;

import javax.swing.JButton;
import javax.swing.JPanel;
import javax.swing.JProgressBar;
import javax.swing.JTextField;
import javax.swing.SwingUtilities;

import client.view.ProgressItem;
import client.view.StatusWindow;
import client.view.WorklistItem;
import network.Sniffer;
import network.SnifferCallback;
import rsa.Factorizer;
import rsa.ProgressTracker;

public class CodeBreaker implements SnifferCallback {
    ExecutorService crackThreadPool = Executors.newFixedThreadPool(2);
    private final JPanel workList;
    private final JPanel progressList;    
    private final JProgressBar mainProgressBar;
    

    // -----------------------------------------------------------------------
    
    private CodeBreaker() {
        StatusWindow w  = new StatusWindow();

        workList        = w.getWorkList();
        progressList    = w.getProgressList();
        mainProgressBar = w.getProgressBar();
        w.enableErrorChecks();
       
        
        
        new Sniffer(this).start();
    }
    
    // -----------------------------------------------------------------------
    
    public static void main(String[] args) throws Exception {

        /*
         * Most Swing operations (such as creating view elements) must be
         * performed in the Swing EDT (Event Dispatch Thread).
         * 
         * That's what SwingUtilities.invokeLater is for.
         */

        SwingUtilities.invokeLater(() -> new CodeBreaker());
        
    }

    // -----------------------------------------------------------------------

    /** Called by a Sniffer thread when an encrypted message is obtained. */
    @Override
    public void onMessageIntercepted(String message, BigInteger n) {
        System.out.println("message intercepted (N=" + n + ")...");
        SwingUtilities.invokeLater(() -> {
	        WorklistItem workListItem = new WorklistItem(n, message); 
	        JButton button = new JButton("Break"); 
	        workListItem.add(button); 
	        workList.add(workListItem);
	        ProgressItem progItem = new ProgressItem(n, message);
	        ProgressItem mainProgItem = new ProgressItem(n, message); 
	        JButton buttonRemove = new JButton("remove"); 
	        
	        Tracker progTrack = new Tracker(progItem, mainProgressBar);
	       
	        
	        //ExecutorService progressThreadPool = Executors.newFixedThreadPool(2); 
	        
	        // Används inte längre
	//        Runnable progressRunnable = () -> {
	//        	//1 000 000 = 100%
	//        	while(progTrack.getTotalProgress() < 1000000) {
	//        		//System.out.println(progTrack.getTotalProgress() );
	//        		progItem.getProgressBar().setValue(progTrack.getTotalProgress());	
	//        	}
	//        	progressList.remove(progItem);
	//        	//System.out.println(Factorizer.crack(message, n, progTrack));
	//        	JTextField text = new JTextField(message); 
	    	
	//       };
	        Runnable crackRunnable = () ->{
	        	String myCode = Factorizer.crack(message, n, progTrack);
	        	SwingUtilities.invokeLater(() -> {

	        	progItem.getTextArea().setEditable(true);
	        	progItem.getTextArea().setText(myCode); 
	            progItem.add(buttonRemove); 
	        	}); 
	        	//progressList.remove(progItem);
	
	        };
	        
	        // Break code knappen
	        button.addActionListener(e -> {
	        	progressList.add(progItem);
	        	workList.remove(workListItem);
	        	crackThreadPool.execute(crackRunnable);
	            mainProgressBar.setMaximum(mainProgressBar.getMaximum()+1000000);
	            
	        	System.out.println("Detta är maxValue från button Break: "+mainProgressBar.getMaximum());
	        	
	        });
	        
	        buttonRemove.addActionListener(e ->{
	        	SwingUtilities.invokeLater(() -> {
	        	progressList.remove(progItem);
	        	mainProgressBar.setValue(mainProgressBar.getValue()-1000000);
	        	mainProgressBar.setMaximum(mainProgressBar.getMaximum()-1000000);
	        	}); 
	        	System.out.println("Detta är maxValue från button remove: "+mainProgressBar.getMaximum());
	
	        	
	        });
	        
	        
	
	        
        });
       }
        
        
        //workList.add(new Component(n));
    private static class Tracker implements ProgressTracker {
        private int totalProgress = 0;
        private int prevPercent = -1;
        ProgressItem progItem; 
        JProgressBar mainProgressBar; 
        
        public Tracker (ProgressItem progItem, JProgressBar mainProgressBar) {
        	this.progItem = progItem; 
        	this.mainProgressBar = mainProgressBar; 
        	
        	
        	
        }
        
        public int getTotalProgress() {
        	return totalProgress; 
        }
        public int getPrevPercent() {
        	return prevPercent; 
        }
        /**
         * Called by Factorizer to indicate progress. The total sum of
         * ppmDelta from all calls will add upp to 1000000 (one million).
         * 
         * @param  ppmDelta   portion of work done since last call,
         *                    measured in ppm (parts per million)
         */
        @Override
        public void onProgress(int ppmDelta) {
            SwingUtilities.invokeLater(() -> {
            	progItem.getProgressBar().setValue(totalProgress);
            	mainProgressBar.setValue(mainProgressBar.getValue()+ppmDelta); 
            });

        	

            totalProgress += ppmDelta;
            int percent = totalProgress / 10000;
            if (percent != prevPercent) {
                //System.out.println(percent + "%");
                prevPercent = percent;
            }
            //progItem.getProgressBar().setValue(totalProgress);
            //SwingUtilities.invokeLater(progItem.getProgressBar().setValue(totalProgress));
            //SwingUtilities.invokeLater
        }
    }
        
    }

