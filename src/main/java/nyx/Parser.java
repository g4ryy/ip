package nyx;

import java.io.IOException;
import java.util.ArrayList;

import nyx.task.Deadline;
import nyx.task.Event;
import nyx.task.Task;
import nyx.task.TaskList;
import nyx.task.ToDo;

/**
 * Deals with making sense of the user input.
 */

public class Parser {
    /**
     * Interprets the user input and executes the command specified in the input using TaskList and Storage objects.
     * @param input String representation of the user input.
     * @param taskList TaskList object that keeps track of all the tasks.
     * @param dataManager Storage object to handle operations with the hard disk.
     * @return String representation of the message to show the user.
     * @throws NyxException If an error has occurred while executing the command.
     */
    public static String parse(String input, TaskList taskList, Storage dataManager) throws NyxException {
        String[] splitInput = input.split(" ", 2);
        String command = splitInput[0];
        String info = "";
        if (splitInput.length > 1) {
            info = splitInput[1].strip();
        }

        switch (command) {
        case "list":
            return taskList.toString();
        case "done":
            try {
                int index = Integer.parseInt(info) - 1;
                if (taskList.markDone(index)) {
                    dataManager.overwriteData(taskList);
                    return String.format("Nice! I've marked this task as done:\n  %s",
                            taskList.getTask(index));
                } else {
                    return ("No task to mark!");
                }
            } catch (IndexOutOfBoundsException | NumberFormatException e) {
                throw new NyxException("Invalid task index!");
            } catch (IOException e) {
                throw new NyxException("Unable to save the changes...");
            }
        case "todo":
            if (info.isEmpty()) {
                throw new NyxException("The description of a todo cannot be empty.");
            }

            ToDo toDo = new ToDo(info);

            try {
                dataManager.addData(toDo);
                taskList.addTask(toDo);
                return String.format("Got it. I've added this task:\n\t  %s\nNow you have %d tasks in the list.",
                        toDo, taskList.getNumTasks());
            } catch (IOException e) {
                throw new NyxException("Unable to save this todo...");
            }
        case "deadline":
            if (info.isEmpty()) {
                throw new NyxException("The description of a deadline cannot be empty.");
            }

            String[] splitInfo = info.split(" /by ");
            Deadline deadline = new Deadline(splitInfo[0].strip(), splitInfo[1]);

            try {
                dataManager.addData(deadline);
                taskList.addTask(deadline);
                return String.format("Got it. I've added this task:%n  %s\nNow you have %d tasks in the list.",
                        deadline, taskList.getNumTasks());
            } catch (IOException e) {
                throw new NyxException("Unable to save this deadline...");
            }
        case "event":
            if (info.isEmpty()) {
                throw new NyxException("The description of an event cannot be empty.");
            }

            splitInfo = info.split(" /at ");
            Event event = new Event(splitInfo[0].strip(), splitInfo[1]);

            try {
                dataManager.addData(event);
                taskList.addTask(event);
                return String.format("Got it. I've added this task:\n  %s\nNow you have %d tasks in the list.",
                        event, taskList.getNumTasks());
            } catch (IOException e) {
                throw new NyxException("Unable to save this event...");
            }
        case "delete":
            try {
                int index = Integer.parseInt(info) - 1;

                if (taskList.getNumTasks() > 0) {
                    Task task = taskList.getTask(index);
                    taskList.removeTask(index);
                    dataManager.overwriteData(taskList);
                    return String.format("Noted! I've removed this task:\n  %s\nNow you have %d tasks in the list",
                            task, taskList.getNumTasks());
                } else {
                    return "No task to delete!";
                }
            } catch (IndexOutOfBoundsException | NumberFormatException e) {
                throw new NyxException("Invalid task index!");
            } catch (IOException e) {
                throw new NyxException("Unable to save the changes...");
            }
        case "bye":
            return "Bye. Hope to see you again soon!";
        case "find":
            if (info.isEmpty()) {
                throw new NyxException("Please enter the keyword to search tasks by...");
            } else {
                ArrayList<Task> filteredTasks = taskList.searchTask(info);
                if (filteredTasks.isEmpty()) {
                    return "No matching tasks found.";
                } else {
                    StringBuilder output = new StringBuilder("Here are the matching tasks in your list:\n");
                    for (int i = 1; i <= filteredTasks.size(); i++) {
                        output.append(String.format("%d. %s\n", i, filteredTasks.get(i - 1)));
                    }
                    return output.toString();
                }
            }
        default:
            throw new NyxException("I dont understand this command... Please try again.");
        }
    }
}
