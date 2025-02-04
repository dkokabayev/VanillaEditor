import controls.text.TextArea
import java.awt.BorderLayout
import java.awt.Color
import javax.swing.*
import javax.swing.filechooser.FileNameExtensionFilter

object EditorSettings {
    const val FONT_NAME = "Monospaced"
    const val FONT_SIZE = 13
    val FONT_COLOR: Color = Color.BLACK
    val SELECTION_COLOR: Color = Color.PINK
    const val SCROLL_BAR_WIDTH = 12
    val SCROLL_BAR_COLOR: Color = Color.lightGray
    val SCROLL_BAR_HOVER_COLOR: Color = Color.gray
    val SCROLL_BAR_DRAG_COLOR = Color(110, 110, 110)
    val SCROLL_BAR_BACKGROUND_COLOR = Color(230, 230, 230)
    val CARET_COLOR: Color = Color.BLACK
    const val LINE_NUMBERS_VISIBLE = true
    val LINE_NUMBERS_COLUMN_COLOR: Color = Color.GRAY
    val LINE_NUMBERS_COLUMN_BACKGROUND_COLOR = Color(230, 230, 230)
    const val EDITOR_WIDTH = 800
    const val EDITOR_HEIGHT = 600
    const val FRAME_TITLE = "Vanilla Editor"
    const val FILE_MENU_TITLE = "File"
    const val OPEN_FILE_TITLE = "Open"
    const val SAVE_FILE_TITLE = "Save"
    const val FILE_FILTER_DESCRIPTION = "java files"
    const val FILE_EXTENSION = "java"
}

fun main() {
    SwingUtilities.invokeLater {
        val frame = JFrame(EditorSettings.FRAME_TITLE)
        frame.defaultCloseOperation = JFrame.EXIT_ON_CLOSE
        frame.setSize(EditorSettings.EDITOR_WIDTH, EditorSettings.EDITOR_HEIGHT)

        val textComponent = TextArea(
            fontName = EditorSettings.FONT_NAME,
            fontSize = EditorSettings.FONT_SIZE,
            fontColor = EditorSettings.FONT_COLOR,
            selectionColor = EditorSettings.SELECTION_COLOR,
            scrollBarWidth = EditorSettings.SCROLL_BAR_WIDTH,
            scrollBarColor = EditorSettings.SCROLL_BAR_COLOR,
            scrollBarHoverColor = EditorSettings.SCROLL_BAR_HOVER_COLOR,
            scrollBarDragColor = EditorSettings.SCROLL_BAR_DRAG_COLOR,
            scrollBarBackgroundColor = EditorSettings.SCROLL_BAR_BACKGROUND_COLOR,
            caretColor = EditorSettings.CARET_COLOR,
            lineNumbersVisible = EditorSettings.LINE_NUMBERS_VISIBLE,
            lineNumbersColumnColor = EditorSettings.LINE_NUMBERS_COLUMN_COLOR,
            lineNumbersColumnBackgroundColor = EditorSettings.LINE_NUMBERS_COLUMN_BACKGROUND_COLOR,
        )

        // TODO: Remove these lines
        textComponent.text = """import java.time.*;

sealed interface Vehicle permits Car {
    record MaintenanceRecord(String type, LocalDateTime date) {}
}

non-sealed class Car implements Vehicle {
    private sealed interface Engine permits ElectricEngine, GasEngine {
        void start();
    }
    private static final class ElectricEngine implements Engine {
        public synchronized void start() {}
    }
    private static final class GasEngine implements Engine {
        public synchronized void start() {}
    }
}

public class LargeJavaClass {
    private volatile int counter;
    private static final String CONSTANT = "280a03c6-6de6-4f3d-8ff4-7a73e47796bf";

    public void demonstrateFeatures() {
        record Point(int x, int y) {}
        var point = new Point(1, 2);
        Object obj = point;
        if (obj instanceof Point p && p.x() > 0) {
            System.out.println(p.y());
        }
        String result = switch(counter) {
            case 1 -> "One";
            case 2 -> {
                yield "Two";
            }
            default -> "Other";
        };
    }
}"""

        frame.add(JScrollPane(textComponent), BorderLayout.CENTER)

        val menuBar = JMenuBar()
        val fileMenu = JMenu(EditorSettings.FILE_MENU_TITLE)

        val openItem = JMenuItem(EditorSettings.OPEN_FILE_TITLE).apply {
            addActionListener {
                val chooser = JFileChooser()
                chooser.fileFilter =
                    FileNameExtensionFilter(EditorSettings.FILE_FILTER_DESCRIPTION, EditorSettings.FILE_EXTENSION)
                if (chooser.showOpenDialog(frame) == JFileChooser.APPROVE_OPTION) {
                    textComponent.text = chooser.selectedFile.readText()
                }
            }
        }

        val saveItem = JMenuItem(EditorSettings.SAVE_FILE_TITLE).apply {
            addActionListener {
                val chooser = JFileChooser()
                chooser.fileFilter =
                    FileNameExtensionFilter(EditorSettings.FILE_FILTER_DESCRIPTION, EditorSettings.FILE_EXTENSION)
                if (chooser.showSaveDialog(frame) == JFileChooser.APPROVE_OPTION) {
                    chooser.selectedFile.writeText(textComponent.text)
                }
            }
        }

        fileMenu.add(openItem)
        fileMenu.add(saveItem)
        menuBar.add(fileMenu)
        frame.jMenuBar = menuBar

        frame.isVisible = true
    }
}