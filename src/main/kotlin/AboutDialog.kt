import java.awt.BorderLayout
import java.awt.Font
import javax.swing.*
import javax.swing.border.EmptyBorder

class AboutDialog(parent: JFrame) : JDialog(parent, "About ${EditorFrame.APP_NAME}", true) {
    init {
        val panel = JPanel(BorderLayout(0, 20)).apply {
            border = EmptyBorder(20, 20, 20, 20)
        }

        val titleLabel = JLabel(EditorFrame.APP_NAME).apply {
            font = font.deriveFont(Font.BOLD, 24f)
            horizontalAlignment = SwingConstants.CENTER
        }
        panel.add(titleLabel, BorderLayout.NORTH)

        val infoPanel = JPanel().apply {
            layout = BoxLayout(this, BoxLayout.Y_AXIS)
        }

        JLabel(EditorFrame.APP_DESCRIPTION).apply {
            alignmentX = 0.5f
            infoPanel.add(this)
            infoPanel.add(Box.createVerticalStrut(10))
        }

        JLabel("Version ${EditorFrame.APP_VERSION}").apply {
            alignmentX = 0.5f
            infoPanel.add(this)
            infoPanel.add(Box.createVerticalStrut(10))
        }

        JLabel("© ${EditorFrame.APP_YEAR} ${EditorFrame.APP_AUTHOR}").apply {
            alignmentX = 0.5f
            infoPanel.add(this)
        }

        panel.add(infoPanel, BorderLayout.CENTER)

        val okButton = JButton("OK").apply {
            addActionListener { dispose() }
        }
        val buttonPanel = JPanel().apply {
            add(okButton)
        }
        panel.add(buttonPanel, BorderLayout.SOUTH)

        contentPane.add(panel)
        pack()
        isResizable = false
        setLocationRelativeTo(parent)
        defaultCloseOperation = DISPOSE_ON_CLOSE
    }
}