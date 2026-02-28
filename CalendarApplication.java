import javax.swing.*;
import javax.swing.border.EmptyBorder;
import java.awt.*;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.Date;
import java.util.List;

public class CalendarApplication extends JFrame {
    private final List<Event> events = new ArrayList<>();
    private final SimpleDateFormat dateFormat = new SimpleDateFormat("yyyy-MM-dd");

    private DefaultListModel<Event> listModel;
    private JList<Event> eventList;
    private JLabel statusLabel;

    public static void main(String[] args) {
        SwingUtilities.invokeLater(() -> {
            setSystemLookAndFeel();
            new CalendarApplication().setVisible(true);
        });
    }

    public CalendarApplication() {
        dateFormat.setLenient(false);

        setTitle("Календарь событий");
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        setMinimumSize(new Dimension(850, 560));
        setLocationRelativeTo(null);

        initComponents();
    }

    private static void setSystemLookAndFeel() {
        try {
            UIManager.setLookAndFeel(UIManager.getSystemLookAndFeelClassName());
        } catch (Exception ignored) {
        }
    }

    private void initComponents() {
        JPanel root = new JPanel(new BorderLayout(12, 12));
        root.setBorder(new EmptyBorder(14, 14, 14, 14));
        root.setBackground(new Color(246, 248, 252));
        setContentPane(root);

        root.add(createHeader(), BorderLayout.NORTH);
        root.add(createCenterPanel(), BorderLayout.CENTER);
        root.add(createFooter(), BorderLayout.SOUTH);

        updateEventList();
    }

    private JComponent createHeader() {
        JPanel header = new JPanel(new BorderLayout());
        header.setOpaque(false);

        JLabel title = new JLabel("Планировщик событий");
        title.setFont(new Font("SansSerif", Font.BOLD, 26));
        title.setForeground(new Color(26, 58, 110));

        JLabel subtitle = new JLabel("Добавляйте, редактируйте и удаляйте события по датам");
        subtitle.setFont(new Font("SansSerif", Font.PLAIN, 14));
        subtitle.setForeground(new Color(90, 100, 120));

        JPanel texts = new JPanel();
        texts.setOpaque(false);
        texts.setLayout(new BoxLayout(texts, BoxLayout.Y_AXIS));
        texts.add(title);
        texts.add(Box.createVerticalStrut(4));
        texts.add(subtitle);

        header.add(texts, BorderLayout.WEST);
        return header;
    }

    private JComponent createCenterPanel() {
        JPanel panel = new JPanel(new BorderLayout(12, 12));
        panel.setOpaque(false);

        listModel = new DefaultListModel<>();
        eventList = new JList<>(listModel);
        eventList.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
        eventList.setCellRenderer(new EventCellRenderer());
        eventList.setFixedCellHeight(58);

        JScrollPane scrollPane = new JScrollPane(eventList);
        scrollPane.setBorder(BorderFactory.createCompoundBorder(
                BorderFactory.createLineBorder(new Color(214, 220, 232)),
                new EmptyBorder(4, 4, 4, 4)
        ));

        panel.add(scrollPane, BorderLayout.CENTER);
        panel.add(createControlPanel(), BorderLayout.EAST);
        return panel;
    }

    private JComponent createControlPanel() {
        JPanel controls = new JPanel();
        controls.setOpaque(false);
        controls.setLayout(new BoxLayout(controls, BoxLayout.Y_AXIS));

        JButton addButton = createActionButton("+ Добавить", new Color(36, 143, 77));
        addButton.addActionListener(e -> handleAdd());

        JButton editButton = createActionButton("✎ Редактировать", new Color(37, 98, 224));
        editButton.addActionListener(e -> handleEdit());

        JButton deleteButton = createActionButton("🗑 Удалить", new Color(191, 57, 57));
        deleteButton.addActionListener(e -> handleDelete());

        controls.add(addButton);
        controls.add(Box.createVerticalStrut(10));
        controls.add(editButton);
        controls.add(Box.createVerticalStrut(10));
        controls.add(deleteButton);
        controls.add(Box.createVerticalGlue());

        return controls;
    }

    private JComponent createFooter() {
        JPanel footer = new JPanel(new BorderLayout());
        footer.setOpaque(false);

        statusLabel = new JLabel("Событий: 0");
        statusLabel.setForeground(new Color(90, 100, 120));

        JLabel hint = new JLabel("Формат даты: yyyy-MM-dd");
        hint.setForeground(new Color(120, 130, 150));

        footer.add(statusLabel, BorderLayout.WEST);
        footer.add(hint, BorderLayout.EAST);
        return footer;
    }

    private JButton createActionButton(String text, Color color) {
        JButton button = new JButton(text);
        button.setFocusPainted(false);
        button.setForeground(Color.WHITE);
        button.setBackground(color);
        button.setBorder(new EmptyBorder(10, 16, 10, 16));
        button.setMaximumSize(new Dimension(180, 42));
        button.setAlignmentX(Component.CENTER_ALIGNMENT);
        return button;
    }

    private void handleAdd() {
        EventFormData data = showEventDialog("Новое событие", null);
        if (data != null) {
            events.add(new Event(data.name(), data.date()));
            updateEventList();
            showMessage("Событие добавлено.");
        }
    }

    private void handleEdit() {
        Event selectedEvent = eventList.getSelectedValue();
        if (selectedEvent == null) {
            showError("Сначала выберите событие для редактирования.");
            return;
        }

        EventFormData data = showEventDialog("Редактирование события", selectedEvent);
        if (data != null) {
            selectedEvent.setName(data.name());
            selectedEvent.setDate(data.date());
            updateEventList();
            showMessage("Событие обновлено.");
        }
    }

    private void handleDelete() {
        Event selectedEvent = eventList.getSelectedValue();
        if (selectedEvent == null) {
            showError("Сначала выберите событие для удаления.");
            return;
        }

        int result = JOptionPane.showConfirmDialog(
                this,
                "Удалить событие \"" + selectedEvent.getName() + "\"?",
                "Подтверждение удаления",
                JOptionPane.YES_NO_OPTION,
                JOptionPane.WARNING_MESSAGE
        );

        if (result == JOptionPane.YES_OPTION) {
            events.remove(selectedEvent);
            updateEventList();
            showMessage("Событие удалено.");
        }
    }

    private EventFormData showEventDialog(String title, Event initialEvent) {
        JTextField nameField = new JTextField();
        JTextField dateField = new JTextField();

        if (initialEvent != null) {
            nameField.setText(initialEvent.getName());
            dateField.setText(initialEvent.getDateStr());
        }

        JPanel form = new JPanel(new GridLayout(0, 1, 6, 6));
        form.add(new JLabel("Название события:"));
        form.add(nameField);
        form.add(new JLabel("Дата (yyyy-MM-dd):"));
        form.add(dateField);

        int option = JOptionPane.showConfirmDialog(
                this,
                form,
                title,
                JOptionPane.OK_CANCEL_OPTION,
                JOptionPane.PLAIN_MESSAGE
        );

        if (option != JOptionPane.OK_OPTION) {
            return null;
        }

        String name = nameField.getText().trim();
        String dateText = dateField.getText().trim();

        if (name.isEmpty()) {
            showError("Название события не может быть пустым.");
            return null;
        }

        try {
            Date date = dateFormat.parse(dateText);
            return new EventFormData(name, date);
        } catch (ParseException e) {
            showError("Некорректная дата. Используйте формат yyyy-MM-dd.");
            return null;
        }
    }

    private void updateEventList() {
        events.sort(Comparator.comparing(Event::getDate));

        listModel.clear();
        for (Event event : events) {
            listModel.addElement(event);
        }

        statusLabel.setText("Событий: " + events.size());
    }

    private void showMessage(String text) {
        statusLabel.setText(text + " Всего событий: " + events.size());
    }

    private void showError(String text) {
        JOptionPane.showMessageDialog(this, text, "Ошибка", JOptionPane.ERROR_MESSAGE);
    }

    private record EventFormData(String name, Date date) {
    }

    private static class EventCellRenderer extends DefaultListCellRenderer {
        private final Color selected = new Color(225, 235, 255);
        private final Color normal = Color.WHITE;

        @Override
        public Component getListCellRendererComponent(
                JList<?> list,
                Object value,
                int index,
                boolean isSelected,
                boolean cellHasFocus
        ) {
            JLabel label = (JLabel) super.getListCellRendererComponent(list, value, index, isSelected, cellHasFocus);
            label.setBorder(new EmptyBorder(10, 10, 10, 10));

            if (value instanceof Event event) {
                label.setText("📅 " + event.getName() + " — " + event.getDateStr());
            }

            label.setOpaque(true);
            label.setBackground(isSelected ? selected : normal);
            label.setForeground(new Color(45, 52, 66));
            return label;
        }
    }
}

class Event {
    private static final SimpleDateFormat DATE_FORMAT = new SimpleDateFormat("yyyy-MM-dd");

    private String name;
    private Date date;

    public Event(String name, Date date) {
        this.name = name;
        this.date = date;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public Date getDate() {
        return date;
    }

    public void setDate(Date date) {
        this.date = date;
    }

    public String getDateStr() {
        return DATE_FORMAT.format(date);
    }

    @Override
    public String toString() {
        return name + " (" + getDateStr() + ")";
    }
}
