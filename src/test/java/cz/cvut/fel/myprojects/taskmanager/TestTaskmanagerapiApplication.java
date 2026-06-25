package cz.cvut.fel.myprojects.taskmanager;

import org.springframework.boot.SpringApplication;

public class TestTaskmanagerapiApplication {

	public static void main(String[] args) {
		SpringApplication.from(TaskManagerApplication::main).with(TestcontainersConfiguration.class).run(args);
	}

}
