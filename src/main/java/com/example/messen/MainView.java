package com.example.messen;

import com.vaadin.flow.component.*;
import com.vaadin.flow.component.button.Button;
import com.vaadin.flow.component.grid.Grid;
import com.vaadin.flow.component.html.Div;
import com.vaadin.flow.component.html.H3;
import com.vaadin.flow.component.orderedlayout.HorizontalLayout;
import com.vaadin.flow.component.orderedlayout.VerticalLayout;
import com.vaadin.flow.component.textfield.TextField;
import com.vaadin.flow.data.renderer.ComponentRenderer;
import com.vaadin.flow.router.Route;
import com.vaadin.flow.shared.Registration;
import lombok.AllArgsConstructor;
import lombok.Getter;
import org.commonmark.node.Node;
import org.commonmark.parser.Parser;
import org.commonmark.renderer.html.HtmlRenderer;

import java.util.Queue;
import java.util.concurrent.ConcurrentLinkedQueue;

@Route("")
public class MainView extends VerticalLayout {
    private final Storage storage;
    private Registration registration;
    private Grid<Storage.ChatMessage> grid;
    private VerticalLayout chat;
    private VerticalLayout login;
    private String user ="";

    public MainView(Storage storage) {
        this.storage = storage;
        buildLogin();
        buildChat();
    }

    private void buildLogin() {
        TextField field =  new TextField();
        field.setPlaceholder("Please, introduse yourself");
        login = new VerticalLayout(){{
            add(
                    field,
                    new Button("Login"){{
                        addClickListener(click -> {
                            login.setVisible(false);
                            chat.setVisible(true);
                            user = field.getValue();
                            storage.addRecordJoined(user);
                        });
                        addClickShortcut(Key.ENTER);
                    }}
            );
        }};
        add(login);
    }

    private void buildChat() {
        chat = new VerticalLayout();
        add(chat);
        chat.setVisible(false);

        grid = new Grid<>();
        grid.setItems(storage.getMessages());
        grid.addColumn(new ComponentRenderer<>(message -> new Html(renderRow(message))))
                .setAutoWidth(true);
        TextField field = new TextField();
        chat.add(new H3("Vaadin chat"),grid,
                new HorizontalLayout(){{
                    add(field,
                            new Button("Send message") {{
                                addClickListener(
                                        click -> {
                                            storage.addRecord(user, field.getValue());
                                            field.clear();
                                        });
                                addClickShortcut(Key.ENTER);
                            }}
                    );
                }}
        );
    }

    private static String renderRow(Storage.ChatMessage message) {
        Parser parser = Parser.builder().build();
        Node document = parser.parse(String.format("**%s**: %s", message.getName(), message.getMessage()));
        HtmlRenderer htmlRenderer = HtmlRenderer.builder().build();
        if (message.getName().isEmpty()) {
            return htmlRenderer.render(parser.parse(String.format("_User **%s** is joined the chat!",  message.getMessage())));
        }else {

            return htmlRenderer.render(document);
        }
    }

    public void onMessage(Storage.ChatEvent event) {
        if (getUI().isPresent()) {
            UI ui = getUI().get();
            ui.getSession().lock();
            ui.access(() -> grid.getDataProvider().refreshAll());
            ui.getPage().executeJs("$0.scrollToIndex($1)",grid,storage.size());
            ui.getSession().unlock();
        }
    }

    @Override
    protected void onAttach(AttachEvent attachEvent) {
        registration = storage.attachListener(this::onMessage);
    }

    @Override
    protected void onDetach(DetachEvent detachEvent) {
        registration.remove();
    }

}
