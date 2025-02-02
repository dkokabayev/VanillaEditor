import controls.text.TextArea
import java.awt.BorderLayout
import java.awt.Color
import javax.swing.*
import javax.swing.filechooser.FileNameExtensionFilter

object EditorSettings {
    val FONT_COLOR: Color = Color.BLACK
    val SELECTION_COLOR: Color = Color.PINK
    const val FONT_NAME = "Monospaced"
    const val FONT_SIZE = 14
    const val PADDING = 5
    const val CARET_BLINK_RATE = 500
    const val BACKSPACE_INITIAL_REPEAT_RATE = 250
    const val EDITOR_WIDTH = 800
    const val EDITOR_HEIGHT = 600
    const val FRAME_TITLE = "Vanilla Editor"
    const val FILE_MENU_TITLE = "File"
    const val OPEN_FILE_TITLE = "Open"
    const val SAVE_FILE_TITLE = "Save"
    const val FILE_FILTER_DESCRIPTION = "java files"
    const val FILE_EXTENSION = "java"
    const val NEW_LINE_CHAR = '\n'
}

fun main() {
    SwingUtilities.invokeLater {
        val frame = JFrame(EditorSettings.FRAME_TITLE)
        frame.defaultCloseOperation = JFrame.EXIT_ON_CLOSE
        frame.setSize(EditorSettings.EDITOR_WIDTH, EditorSettings.EDITOR_HEIGHT)

        val textComponent = TextArea(
            fontName = EditorSettings.FONT_NAME,
            fontSize = EditorSettings.FONT_SIZE,
            caretBlinkRate = EditorSettings.CARET_BLINK_RATE,
            backspaceInitialRepeatRate = EditorSettings.BACKSPACE_INITIAL_REPEAT_RATE,
            newLineChar = EditorSettings.NEW_LINE_CHAR,
            fontColor = EditorSettings.FONT_COLOR,
            selectionColor = EditorSettings.SELECTION_COLOR,
            padding = EditorSettings.PADDING,
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
}""";

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