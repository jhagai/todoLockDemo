package fr.jhagai.todoLockDemo.web;

import fr.jhagai.todoLockDemo.core.dao.UserRepository;
import fr.jhagai.todoLockDemo.core.entities.User;
import fr.jhagai.todoLockDemo.core.services.TodoService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.ApplicationArguments;
import org.springframework.boot.ApplicationRunner;
import org.springframework.stereotype.Component;

import javax.transaction.Transactional;

@Component
public class DataInit implements ApplicationRunner {

    @Autowired
    private TodoService todoService;

    @Autowired
    private UserRepository userRepository;

    @Override
    @Transactional
    public void run(ApplicationArguments args) throws Exception {

        final User cyril = new User();
        cyril.setLogin("cyril");
        cyril.setPassword("balit");

        this.userRepository.save(cyril);

        final User arnaud = new User();
        arnaud.setLogin("arnaud");
        arnaud.setPassword("waller");

        this.userRepository.save(arnaud);

        todoService.addTodo("Augmenter Joël", "@TODO: Make it rain baby $$$");
    }

}
