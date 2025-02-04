import java.awt.Desktop
import javax.swing.JFrame
import javax.swing.SwingUtilities

fun main() {
    if (System.getProperty("os.name").lowercase().contains("mac")) {
        System.setProperty("apple.awt.application.name", EditorFrame.APP_NAME)
        Desktop.getDesktop().apply {
            setAboutHandler {
                SwingUtilities.invokeLater {
                    val frame = WindowManager.getMainFrame()
                    if (frame is EditorFrame) {
                        AboutDialog(frame).isVisible = true
                    }
                }
            }
        }
    }

    JFrame.setDefaultLookAndFeelDecorated(true)
    SwingUtilities.invokeLater {
        EditorFrame().apply {
            isVisible = true
        }
    }
}