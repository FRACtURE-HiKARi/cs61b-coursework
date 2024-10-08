package gitlet;

import java.io.File;
import java.io.IOException;
import java.io.OutputStream;
import java.io.OutputStreamWriter;
import java.util.Objects;

/** Driver class for Gitlet, a subset of the Git version-control system.
 *  @author TODO
 */
public class Main {

    /** Usage: java gitlet.Main ARGS, where ARGS contains
     *  <COMMAND> <OPERAND1> <OPERAND2> ... 
     */
    public static void main(String[] args) {
        // TODO: what if args is empty?
        if (args.length == 0) {
            System.out.println("Please enter a command.");
            System.exit(0);
        }

        Repository repo = new Repository();
        String firstArg = args[0];
        if (firstArg.equals("init")) {
            repo.init();
        } else {
            if (!Repository.GITLET_FILE.exists()) {
                System.out.println("Not in an initialized Gitlet directory.");
                System.exit(0);
            }
            switch (firstArg) {
                case "add":
                    repo.add(args[1]);
                    break;
                case "commit":
                    String user = "61b-student";
                    repo.makeCommit(args[1], user);
                    break;
                case "rm":
                    repo.remove(args[1]);
                    break;
                case "log":
                    repo.log();
                    break;
                case "global-log":
                    repo.globalLog();
                    break;
                case "find":
                    repo.find(args[1]);
                    break;
                case "status":
                    repo.status();
                    break;
                // TODO: different checkout cases
                case "checkout":
                    if (args.length == 4) {
                        Utils.exitOnCondition(!Objects.equals(args[2], "--"), "Incorrect operands.");
                        repo.checkout(args[1], args[3]);
                    } else if (args.length == 3) {
                        Utils.exitOnCondition(!Objects.equals(args[1], "--"), "Incorrect operands.");
                        repo.checkoutFile(args[2]);
                    } else if (args.length == 2) {
                        repo.checkoutBranch(args[1]);
                    }
                    break;
                case "branch":
                    repo.branch(args[1]);
                    break;
                case "rm-branch":
                    repo.rmBranch(args[1]);
                    break;
                case "reset":
                    repo.reset(args[1]);
                    break;
                case "merge":
                    repo.merge(args[1]);
                    break;
                case "add-remote":
                    repo.addRemote(args[1], args[2]);
                    break;
                case "rm-remote":
                    repo.rmRemote(args[1]);
                    break;
                case "pull":
                    repo.pull(args[1], args[2]);
                    break;
                case "fetch":
                    repo.fetch(args[1], args[2]);
                    break;
                case "push":
                    repo.push(args[1], args[2]);
                    break;
                case "db":
                    repo.debug();
                    break;
                default:
                    System.out.println("No command with that name exists.");
                    System.exit(0);
            }
            // TODO: FILL THE REST IN
        }
        repo.saveState();
    }

    public static void printUsage() {
        // TODO: Useage Message
        System.out.println(
                  "Usage:\n"
                + "java gitlet.Main [command]\n"
                + "Supports commands:\n"
                + "    init                 Creates a new gitlet control in the folder\n"
                + "    add [file]           Adds a file to version control\n"
                + "    commit [message]     Saves a snapshot of tracked files in the current commit.\n"
                + "    rm [file]            Unstage the file if it is currently staged for addition.\n"
                + "    log                  Display information about each commit.\n"
                + "    global-log           Like log, except displays information about all commits ever made.\n"
                + "    find [msg]           Prints out the ids of all commits that have the given commit message.\n"
                + "    status               Displays what branches currently exist, and marks the current branch with a *.\n"
                + "    checkout [file] / [commit id] -- [file] / [branch name]\n"
                + "    branch [branch]      Creates and switch to a new branch with the given name.\n"
                + "    rm-branch [name]     Deletes the branch with the given name.\n"
                + "    reset [commit id]    Checks out all the files tracked by the given commit.\n"
                + "    merge [branch]       Merges files from the given branch into the current branch.\n"
                + "\n"
                + "    help                 Prints the help message.\n"
                + "Remote Commands:\n"
                + "    add-remote [name] [path] adds a remote repo\n"
                + "    rm-remote [name]         removes a remote repo\n"
                + "    pull [remote] [branch]   pull commits from the remote repo and try merging\n"
                + "    fetch [remote] [branch]  fetch file from the remote branch but not merging\n"
                + "    push [remote]            push commits to the remote repo and try merging\n"
        );
    }

    public static void checkArgs(String[] args, int length) {

    }
}
