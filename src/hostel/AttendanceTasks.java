package hostel;

import javafx.application.Platform;

public class AttendanceTasks {

    public interface StatusCallback {
        void onUpdate(String message);
        void onVerificationComplete(Student student);
        void onAlertComplete(Student student);
        void onLeaveProcessed(Student student);
    }

    public interface ClockCallback {
        void onTick();
    }

    public static class MarkPresentTask implements Runnable {
        private final Student student;
        private final StatusCallback callback;

        public MarkPresentTask(Student student, StatusCallback callback) {
            this.student = student;
            this.callback = callback;
        }

        @Override
        public void run() {
            try {
                Platform.runLater(() -> callback.onUpdate("Initiating biometric verification for " + student.getName() + "..."));
                Thread.sleep(1000);

                Platform.runLater(() -> callback.onUpdate("Scanning fingerprint..."));
                Thread.sleep(1500);

                Platform.runLater(() -> callback.onUpdate("Verifying identity with database..."));
                Thread.sleep(1000);

                student.setStatus(Student.Status.PRESENT);
                Platform.runLater(() -> {
                    callback.onUpdate("Attendance marked PRESENT for " + student.getName() + ".");
                    callback.onVerificationComplete(student);
                });

            } catch (InterruptedException e) {
                Thread.currentThread().interrupt();
                Platform.runLater(() -> callback.onUpdate("Verification interrupted for " + student.getName()));
            }
        }
    }

    public static class MarkAbsentTask implements Runnable {
        private final Student student;
        private final StatusCallback callback;

        public MarkAbsentTask(Student student, StatusCallback callback) {
            this.student = student;
            this.callback = callback;
        }

        @Override
        public void run() {
            try {
                Platform.runLater(() -> callback.onUpdate("Flagging " + student.getName() + " as ABSENT..."));
                Thread.sleep(1000);

                Platform.runLater(() -> callback.onUpdate("Generating SMS alert for parents..."));
                Thread.sleep(1500);

                Platform.runLater(() -> callback.onUpdate("Dispatching alert via gateway..."));
                Thread.sleep(1000);

                student.setStatus(Student.Status.ABSENT);
                Platform.runLater(() -> {
                    callback.onUpdate("Alert sent. " + student.getName() + " marked ABSENT.");
                    callback.onAlertComplete(student);
                });

            } catch (InterruptedException e) {
                Thread.currentThread().interrupt();
                Platform.runLater(() -> callback.onUpdate("Task interrupted for " + student.getName()));
            }
        }
    }

    public static class MarkLeaveTask implements Runnable {
        private final Student student;
        private final StatusCallback callback;

        public MarkLeaveTask(Student student, StatusCallback callback) {
            this.student = student;
            this.callback = callback;
        }

        @Override
        public void run() {
            try {
                Platform.runLater(() -> callback.onUpdate("Processing leave application for " + student.getName() + "..."));
                Thread.sleep(1200);

                Platform.runLater(() -> callback.onUpdate("Updating hostel ledger..."));
                Thread.sleep(1200);

                student.setStatus(Student.Status.ON_LEAVE);
                Platform.runLater(() -> {
                    callback.onUpdate(student.getName() + " is currently ON LEAVE.");
                    callback.onLeaveProcessed(student);
                });

            } catch (InterruptedException e) {
                Thread.currentThread().interrupt();
                Platform.runLater(() -> callback.onUpdate("Leave processing interrupted for " + student.getName()));
            }
        }
    }

    public static class ClockUpdater implements Runnable {
        private volatile boolean running = true;
        private final ClockCallback callback;

        public ClockUpdater(ClockCallback callback) {
            this.callback = callback;
        }

        public void stop() { running = false; }

        @Override
        public void run() {
            while (running) {
                try {
                    Thread.sleep(1000); // More frequent updates if doing clock
                    Platform.runLater(callback::onTick);
                } catch (InterruptedException e) {
                    Thread.currentThread().interrupt();
                    break;
                }
            }
        }
    }
}
