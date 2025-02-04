import javax.swing.JFrame

object WindowManager {
    private var mainFrame: JFrame? = null

    fun setMainFrame(frame: JFrame) {
        mainFrame = frame
    }

    fun getMainFrame(): JFrame? = mainFrame
}