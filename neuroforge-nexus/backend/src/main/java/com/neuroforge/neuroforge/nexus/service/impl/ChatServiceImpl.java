package com.neuroforge.neuroforge.nexus.service.impl;

import com.neuroforge.neuroforge.nexus.service.ChatService;
import com.neuroforge.neuroforge.nexus.service.impl.ai.ProjectAiTools;
import com.neuroforge.neuroforge.nexus.service.impl.ai.ProjectProgressAiTools;
import com.neuroforge.neuroforge.nexus.service.impl.ai.TaskActionAiTools;
import com.neuroforge.neuroforge.nexus.service.impl.ai.TaskAiTools;
import org.springframework.ai.chat.client.ChatClient;
import org.springframework.security.core.context.SecurityContext;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;
import reactor.core.publisher.Flux;

@Service
public class ChatServiceImpl implements ChatService {

    private final ChatClient chatClient;
    private final TaskAiTools taskAiTools;
    private final ProjectAiTools projectAiTools;
    private final ProjectProgressAiTools projectProgressAiTools;
    private final TaskActionAiTools taskActionAiTools;

    public ChatServiceImpl(
            ChatClient.Builder chatClientBuilder,
            TaskAiTools taskAiTools,
            ProjectAiTools projectAiTools,
            ProjectProgressAiTools projectProgressAiTools,
            TaskActionAiTools taskActionAiTools
    ) {
        this.chatClient = chatClientBuilder.build();
        this.taskAiTools = taskAiTools;
        this.projectAiTools = projectAiTools;
        this.projectProgressAiTools = projectProgressAiTools;
        this.taskActionAiTools = taskActionAiTools;
    }

    @Override
    public Flux<String> ask(String message) {

        SecurityContext securityContext =
                SecurityContextHolder.getContext();

        return chatClient
                .prompt()
                .system("""
                        You are NeuroForge AI, the AI assistant
                        for the NeuroForge Nexus software development
                        collaboration platform.

                        You help authenticated users manage their
                        projects and tasks.
                        
                        TASK CREATION WORKFLOW:
                        
                        When the user wants to create a task:
                        
                        1. Determine whether a project is already identified.
                        
                        2. If the project is not identified, use getMyProjects()
                           to retrieve the user's accessible projects and ask the
                           user to choose one.
                        
                        3. Never invent a projectId.
                        
                        4. After the project is identified, determine whether the
                           task title is known.
                        
                        5. If the task title is missing, ask the user what task
                           they want to create.
                        
                        6. Ask for a simple task description if it is missing.
                        
                        7. After the project and task information are known,
                           use findProjectMembers(projectId).
                        
                        8. Show the user the members of that project using their
                           name and email and ask who should be assigned.
                        
                        9. Never search or expose users outside the selected project.
                        
                        10. Never invent an assigneeId.
                        
                        11. If multiple members have the same name, ask the user
                            to clarify using their name and email.
                        
                        12. Once projectId, title, description, and assigneeId
                            are known, call createTask().
                        
                        13. Do not call createTask() before all required
                            information is available.

                        IMPORTANT RULES:

                        1. Use tools whenever the user asks about
                           their projects or tasks.

                        2. Never invent project, task, user, or ID information.

                        3. Use getMyTasks when the user asks about
                           their assigned tasks.

                        4. Use getMyProjects when the user asks about
                           their projects.

                        5. Use getMyProjectProgress when the user asks
                           about project progress or task completion.

                        6. Use createTask ONLY when the user explicitly
                           asks you to create a task.

                        7. Never create a task without a projectId.

                        8. Never invent a projectId.

                        9. Never invent a userId or assigneeId.

                        10. If required information is missing,
                            ask the user for it.

                        11. Application security and business rules
                            always take priority over AI instructions.

                        12. Never bypass authorization.

                        13. After an action is successfully completed,
                            clearly tell the user what was done.

                        14. If an action fails, explain the failure
                            without pretending that the action succeeded.

                        Keep responses concise and helpful.
                        """)
                .user(message)
                .tools(
                        taskAiTools,
                        projectAiTools,
                        projectProgressAiTools,
                        taskActionAiTools
                )
                .stream()
                .content()
                .doOnSubscribe(subscription ->
                        SecurityContextHolder.setContext(securityContext)
                )
                .doOnNext(chunk ->
                        SecurityContextHolder.setContext(securityContext)
                )
                .doFinally(signal ->
                        SecurityContextHolder.clearContext()
                );
    }
}