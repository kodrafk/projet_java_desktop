package tn.esprit.projet.utils;

import com.github.sarxos.webcam.Webcam;
import com.github.sarxos.webcam.WebcamResolution;

import java.awt.Dimension;
import java.util.List;

/**
 * Camera diagnostic tool
 */
public class CameraDiagnostic {

    public static void main(String[] args) {
        System.out.println("╔════════════════════════════════════════════════════════════════╗");
        System.out.println("║              CAMERA DIAGNOSTIC TOOL                            ║");
        System.out.println("╚════════════════════════════════════════════════════════════════╝");
        System.out.println();
        
        try {
            // Get all available webcams
            System.out.println("📊 Step 1: Detecting cameras...");
            List<Webcam> webcams = Webcam.getWebcams();
            
            if (webcams.isEmpty()) {
                System.out.println("   ❌ No cameras detected!");
                System.out.println();
                System.out.println("   Possible causes:");
                System.out.println("   • Camera driver not installed");
                System.out.println("   • Camera disabled in Device Manager");
                System.out.println("   • Camera not connected (external webcam)");
                System.out.println();
                System.out.println("   Solutions:");
                System.out.println("   1. Check Device Manager → Cameras");
                System.out.println("   2. Reinstall camera driver");
                System.out.println("   3. Connect external webcam");
                return;
            }
            
            System.out.println("   ✅ Found " + webcams.size() + " camera(s)");
            System.out.println();
            
            // Test each camera
            for (int i = 0; i < webcams.size(); i++) {
                Webcam webcam = webcams.get(i);
                System.out.println("📷 Camera " + (i + 1) + ": " + webcam.getName());
                System.out.println("   ─────────────────────────────────────");
                
                // Check if camera is open
                if (webcam.isOpen()) {
                    System.out.println("   Status: ⚠️  ALREADY OPEN (in use by another app)");
                    System.out.println("   → Close Teams, Zoom, Chrome, or other apps using camera");
                } else {
                    System.out.println("   Status: ✅ Available");
                }
                
                // Get supported resolutions
                Dimension[] sizes = webcam.getViewSizes();
                System.out.println("   Supported resolutions: " + sizes.length);
                
                // Show common resolutions
                System.out.println("   Common resolutions:");
                if (supportsResolution(sizes, WebcamResolution.VGA.getSize())) {
                    System.out.println("      ✅ VGA (640x480)");
                }
                if (supportsResolution(sizes, WebcamResolution.HD.getSize())) {
                    System.out.println("      ✅ HD (1280x720)");
                }
                if (supportsResolution(sizes, new Dimension(1920, 1080))) {
                    System.out.println("      ✅ Full HD (1920x1080)");
                }
                
                // Try to open camera
                System.out.println("   Testing camera access...");
                try {
                    if (!webcam.isOpen()) {
                        webcam.setViewSize(WebcamResolution.VGA.getSize());
                        webcam.open();
                        
                        if (webcam.isOpen()) {
                            System.out.println("   ✅ Camera opened successfully!");
                            
                            // Try to capture a frame
                            System.out.println("   Testing frame capture...");
                            if (webcam.getImage() != null) {
                                System.out.println("   ✅ Frame captured successfully!");
                                System.out.println("   → This camera is working perfectly!");
                            } else {
                                System.out.println("   ❌ Failed to capture frame");
                            }
                            
                            webcam.close();
                        } else {
                            System.out.println("   ❌ Failed to open camera");
                        }
                    }
                } catch (Exception e) {
                    System.out.println("   ❌ Error: " + e.getMessage());
                    System.out.println("   → Camera may be in use by another application");
                }
                
                System.out.println();
            }
            
            // Recommendations
            System.out.println("╔════════════════════════════════════════════════════════════════╗");
            System.out.println("║                    RECOMMENDATIONS                             ║");
            System.out.println("╚════════════════════════════════════════════════════════════════╝");
            System.out.println();
            
            boolean hasOpenCamera = false;
            for (Webcam w : webcams) {
                if (w.isOpen()) {
                    hasOpenCamera = true;
                    break;
                }
            }
            
            if (hasOpenCamera) {
                System.out.println("⚠️  CAMERA IN USE");
                System.out.println();
                System.out.println("Your camera is being used by another application.");
                System.out.println();
                System.out.println("Close these apps:");
                System.out.println("   • Microsoft Teams");
                System.out.println("   • Zoom");
                System.out.println("   • Skype");
                System.out.println("   • Google Chrome (tabs with camera)");
                System.out.println("   • OBS Studio");
                System.out.println();
                System.out.println("Then restart NutriLife application.");
            } else {
                System.out.println("✅ ALL CAMERAS AVAILABLE");
                System.out.println();
                System.out.println("Your cameras are ready to use!");
                System.out.println();
                System.out.println("If Face ID still doesn't work:");
                System.out.println("   1. Restart NutriLife application");
                System.out.println("   2. Check camera permissions in Windows Settings");
                System.out.println("   3. Try external webcam if available");
            }
            
        } catch (Exception e) {
            System.err.println("❌ Error during diagnostic: " + e.getMessage());
            e.printStackTrace();
        }
    }
    
    private static boolean supportsResolution(Dimension[] sizes, Dimension target) {
        for (Dimension size : sizes) {
            if (size.width == target.width && size.height == target.height) {
                return true;
            }
        }
        return false;
    }
}
