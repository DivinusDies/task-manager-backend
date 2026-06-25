package cz.cvut.fel.myprojects.taskmanager.exception;

public class AccessDeniedException extends RuntimeException {

    public AccessDeniedException(String message){
        super(message);
    }
}
