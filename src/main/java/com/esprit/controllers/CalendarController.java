package com.esprit.controllers;

import javafx.fxml.FXML;
import javafx.geometry.Pos;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.layout.GridPane;

import java.time.LocalDate;
import java.time.YearMonth;
import java.time.format.DateTimeFormatter;

public class CalendarController {
    @FXML private Label monthYearLabel;
    @FXML private GridPane calendarGrid;
    @FXML private Button prevButton;
    @FXML private Button nextButton;
    @FXML private Label selectedDateRangeLabel;
    @FXML private Button clearFilterButton;
    
    private LocalDate currentDate = LocalDate.now();
    private YearMonth currentYearMonth;
    private LocalDate startDate = null;
    private LocalDate endDate = null;
    private ForumMainController mainController;
    private DateSelectionCallback dateSelectionCallback;
    
    public void setMainController(ForumMainController controller) {
        this.mainController = controller;
    }
    
    @FXML
    public void initialize() {
        currentYearMonth = YearMonth.from(currentDate);
        updateCalendar();
        
        prevButton.setOnAction(e -> {
            currentYearMonth = currentYearMonth.minusMonths(1);
            updateCalendar();
            updateMonthYearLabel();
        });
        
        nextButton.setOnAction(e -> {
            currentYearMonth = currentYearMonth.plusMonths(1);
            updateCalendar();
            updateMonthYearLabel();
        });

        clearFilterButton.setOnAction(e -> clearDateFilter());
        updateSelectedDateRangeLabel();
        
        // Set initial month/year label
        updateMonthYearLabel();
    }

    @FXML
    private void clearDateFilter() {
        // Reset the dates
        startDate = null;
        endDate = null;
        
        // Update the label
        updateSelectedDateRangeLabel();
        
        // Reset calendar visual selection
        updateCalendar();
        
        // Notify main controller to reset posts
        if (dateSelectionCallback != null) {
            dateSelectionCallback.onDateSelected(null, null);
        }
        
        if (mainController != null) {
            mainController.filterPostsByDate(null, null);
        }
        
        System.out.println("Calendar filter cleared");
    }
    
    private void updateSelectedDateRangeLabel() {
        if (startDate == null && endDate == null) {
            selectedDateRangeLabel.setText("No date filter");
            return;
        }
        
        DateTimeFormatter formatter = DateTimeFormatter.ofPattern("MMM d, yyyy");
        if (startDate != null && endDate != null) {
            selectedDateRangeLabel.setText(String.format("From %s to %s", 
                startDate.format(formatter), endDate.format(formatter)));
        } else if (startDate != null) {
            selectedDateRangeLabel.setText(startDate.format(formatter));
        }
    }
    
    private void updateCalendar() {
        monthYearLabel.setText(currentYearMonth.format(DateTimeFormatter.ofPattern("MMMM yyyy")));
        calendarGrid.getChildren().clear();
        
        // Add day labels
        String[] dayNames = {"Sun", "Mon", "Tue", "Wed", "Thu", "Fri", "Sat"};
        for (int i = 0; i < 7; i++) {
            Label dayLabel = new Label(dayNames[i]);
            dayLabel.setStyle("-fx-font-weight: bold; -fx-text-fill: #666666;");
            dayLabel.setAlignment(Pos.CENTER);
            calendarGrid.add(dayLabel, i, 0);
        }
        
        LocalDate firstOfMonth = currentYearMonth.atDay(1);
        int dayOfWeek = firstOfMonth.getDayOfWeek().getValue() % 7;
        
        for (int i = 1; i <= currentYearMonth.lengthOfMonth(); i++) {
            LocalDate date = currentYearMonth.atDay(i);
            Button dateBtn = new Button(String.valueOf(i));
            dateBtn.setMaxWidth(Double.MAX_VALUE);
            dateBtn.setMaxHeight(Double.MAX_VALUE);
            
            // Style the button based on selection state
            updateDateButtonStyle(dateBtn, date);
            
            dateBtn.setOnAction(e -> handleDateSelection(date));
            
            calendarGrid.add(dateBtn, (dayOfWeek + i - 1) % 7, (dayOfWeek + i - 1) / 7 + 1);
        }
    }

    private void handleDateSelection(LocalDate selectedDate) {
        System.out.println("Date selected: " + selectedDate); // Debug print
        
        if (startDate == null || (endDate != null || selectedDate.isBefore(startDate))) {
            // Start new selection
            startDate = selectedDate;
            endDate = null;
        } else {
            // Complete the range
            if (selectedDate.isAfter(startDate)) {
                endDate = selectedDate;
            } else {
                endDate = startDate;
                startDate = selectedDate;
            }
        }
        
        updateSelectedDateRangeLabel();
        updateCalendar();
        
        if (mainController != null) {
            System.out.println("Calling filterPostsByDate with: " + startDate + " to " + endDate); // Debug print
            mainController.filterPostsByDate(startDate, endDate);
        } else {
            System.out.println("mainController is null!"); // Debug print
        }
        
        if (dateSelectionCallback != null) {
            dateSelectionCallback.onDateSelected(startDate, endDate);
        }
    }
    
    private void updateDateButtonStyle(Button dateBtn, LocalDate date) {
        boolean isSelected = (startDate != null && date.equals(startDate)) ||
                           (endDate != null && date.equals(endDate));
        boolean isInRange = startDate != null && endDate != null &&
                          date.isAfter(startDate) && date.isBefore(endDate);
        boolean isToday = date.equals(LocalDate.now());
        
        // Create the base style that won't be modified
        final String baseStyle = "-fx-background-radius: 5; " +
            (isSelected ? "-fx-background-color: #FFE5EC; -fx-text-fill: #333333; -fx-font-weight: bold;" :
             isInRange ? "-fx-background-color: #FFF5F7; -fx-text-fill: #333333;" :
             isToday ? "-fx-background-color: #E6E6E6; -fx-text-fill: #333333;" :
                       "-fx-background-color: transparent; -fx-text-fill: #333333;");
        
        dateBtn.setStyle(baseStyle);
        
        // Add hover effect using the baseStyle
        dateBtn.setOnMouseEntered(e -> {
            if (!isSelected && !isInRange) {
                dateBtn.setStyle(baseStyle + "-fx-background-color: #f0f0f0;");
            }
        });
        
        dateBtn.setOnMouseExited(e -> {
            dateBtn.setStyle(baseStyle);
        });
    }

    private void updateMonthYearLabel() {
        if (monthYearLabel != null) {
            // Format the date to show just month and year
            String monthYear = currentYearMonth.format(
                DateTimeFormatter.ofPattern("MMMM yyyy")
            );
            monthYearLabel.setText(monthYear);
        }
    }

    public interface DateSelectionCallback {
        void onDateSelected(LocalDate startDate, LocalDate endDate);
    }

    public void setOnDateSelected(DateSelectionCallback callback) {
        // Call this when dates are selected in your calendar
        this.dateSelectionCallback = callback;
    }
} 