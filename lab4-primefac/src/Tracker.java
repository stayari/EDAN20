import javax.swing.JProgressBar;
import javax.swing.SwingUtilities;

import client.view.ProgressItem;
import rsa.Factorizer;
import rsa.ProgressTracker;
public class Tracker implements ProgressTracker {
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
        totalProgress += ppmDelta;
        int percent = totalProgress / 10000;
        if (percent != prevPercent) {
            System.out.println(percent + "%");
            prevPercent = percent;
        }
        mainProgressBar.setValue(100000);
        progItem.getProgressBar().setValue(totalProgress);
        //SwingUtilities.invokeLater(progItem.getProgressBar().setValue(totalProgress));
    }
}