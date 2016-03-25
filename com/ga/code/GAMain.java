package com.ga.code;
import com.ga.view.MainFrame;
import java.util.logging.Level;
import java.util.logging.Logger;
/**
 *
 * @author gxw
 */
public class GAMain {
    @SuppressWarnings("deprecation")
	public static void main(String[] args) {
      
        try {
//            javax.swing.UIManager.setLookAndFeel("swing.addon.plaf.threeD.ThreeDLookAndFeel");
//            org.jb2011.lnf.beautyeye.BeautyEyeLNFHelper.launchBeautyEyeLNF();
            javax.swing.UIManager.setLookAndFeel("com.jtattoo.plaf.mcwin.McWinLookAndFeel");
            
            MainFrame mf = new MainFrame();
            mf.show();
        } catch (Exception ex) {
            Logger.getLogger(GAMain.class.getName()).log(Level.SEVERE, null, ex);
        }
    }
}
