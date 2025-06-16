import java.io.*;
import java.nio.file.*;
import java.util.*;

public class TaskTracker {

    private static final String FILE_NAME = "tasks.json";

    public static void main(String[] args) throws IOException {
        if (args.length < 1) {
            System.out.println("Please provide a valid command.");
            return;
        }

        String command = args[0];
        List<Task> tasks = loadTasks();

        switch (command.toLowerCase()) {
            case "add":
                if (args.length < 2) {
                    System.out.println("Usage: add <task_title>");
                    return;
                }
                String title = args[1];
                int id = getNextId(tasks);
                tasks.add(new Task(id, title, "not done"));
                break;

            case "update":
                if (args.length < 3) {
                    System.out.println("Usage: update <id> <new_title>");
                    return;
                }
                int updateId = Integer.parseInt(args[1]);
                String newTitle = args[2];
                tasks.stream().filter(t -> t.id == updateId).forEach(t -> t.title = newTitle);
                break;

            case "delete":
                if (args.length < 2) {
                    System.out.println("Usage: delete <id>");
                    return;
                }
                int deleteId = Integer.parseInt(args[1]);
                tasks.removeIf(t -> t.id == deleteId);
                break;

            case "mark":
                if (args.length < 3) {
                    System.out.println("Usage: mark <id> <status>");
                    return;
                }
                int markId = Integer.parseInt(args[1]);
                String status = args[2].toLowerCase();
                if (!Arrays.asList("done", "not done", "in progress").contains(status)) {
                    System.out.println("Status must be: done, not done, or in progress");
                    return;
                }
                tasks.stream().filter(t -> t.id == markId).forEach(t -> t.status = status);
                break;

            case "list":
                if (args.length == 1) {
                    tasks.forEach(System.out::println);
                } else {
                    String filter = args[1];
                    tasks.stream().filter(t -> t.status.equalsIgnoreCase(filter)).forEach(System.out::println);
                }
                break;

            default:
                System.out.println("Unknown command");
        }

        saveTasks(tasks);
    }

    // Loads tasks from JSON file
    private static List<Task> loadTasks() {
        List<Task> tasks = new ArrayList<>();
        try {
            if (!Files.exists(Paths.get(FILE_NAME))) {
                Files.write(Paths.get(FILE_NAME), "[]".getBytes());
                return tasks;
            }

            String content = new String(Files.readAllBytes(Paths.get(FILE_NAME)));
            content = content.trim();
            if (content.equals("[]")) return tasks;

            content = content.substring(1, content.length() - 1); // remove [ ]
            String[] entries = content.split("\\},\\s*\\{");

            for (String entry : entries) {
                String json = entry;
                if (!entry.startsWith("{")) json = "{" + json;
                if (!entry.endsWith("}")) json = json + "}";

                int id = Integer.parseInt(json.split("\"id\":")[1].split(",")[0].trim());
                String title = json.split("\"title\":")[1].split(",")[0].replaceAll("\"", "").trim();
                String status = json.split("\"status\":")[1].replaceAll("[\"}]", "").trim();

                tasks.add(new Task(id, title, status));
            }

        } catch (IOException e) {
            System.out.println("Failed to load tasks: " + e.getMessage());
        }

        return tasks;
    }

    // Saves tasks to JSON file
    private static void saveTasks(List<Task> tasks) {
        StringBuilder sb = new StringBuilder();
        sb.append("[\n");
        for (int i = 0; i < tasks.size(); i++) {
            sb.append("  ").append(tasks.get(i).toJson());
            if (i != tasks.size() - 1) sb.append(",");
            sb.append("\n");
        }
        sb.append("]");
        try {
            Files.write(Paths.get(FILE_NAME), sb.toString().getBytes());
        } catch (IOException e) {
            System.out.println("Failed to save tasks: " + e.getMessage());
        }
    }

    private static int getNextId(List<Task> tasks) {
        return tasks.stream().mapToInt(t -> t.id).max().orElse(0) + 1;
    }

    static class Task {
        int id;
        String title;
        String status;

        Task(int id, String title, String status) {
            this.id = id;
            this.title = title;
            this.status = status;
        }

        String toJson() {
            return String.format("{\"id\": %d, \"title\": \"%s\", \"status\": \"%s\"}", id, title, status);
        }

        public String toString() {
            return String.format("Task %d: %s [%s]", id, title, status);
        }
    }
}
