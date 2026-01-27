package com.geysermenu.extension.network.protocol;

import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

/**
 * Represents menu data sent from companion plugin to extension
 */
public class MenuData {

    private String formId;
    private UUID targetPlayer;
    private MenuType type;
    private String title;
    private String content;
    private List<Button> buttons = new ArrayList<>();
    private List<FormComponent> components = new ArrayList<>();

    public MenuData() {}

    public MenuData(String formId, UUID targetPlayer, MenuType type, String title) {
        this.formId = formId;
        this.targetPlayer = targetPlayer;
        this.type = type;
        this.title = title;
    }

    public enum MenuType {
        SIMPLE,     // SimpleForm - Button list
        MODAL,      // ModalForm - Yes/No dialog
        CUSTOM      // CustomForm - Inputs, toggles, sliders, etc.
    }

    public String getFormId() {
        return formId;
    }

    public void setFormId(String formId) {
        this.formId = formId;
    }

    public UUID getTargetPlayer() {
        return targetPlayer;
    }

    public void setTargetPlayer(UUID targetPlayer) {
        this.targetPlayer = targetPlayer;
    }

    public MenuType getType() {
        return type;
    }

    public void setType(MenuType type) {
        this.type = type;
    }

    public String getTitle() {
        return title;
    }

    public void setTitle(String title) {
        this.title = title;
    }

    public String getContent() {
        return content;
    }

    public void setContent(String content) {
        this.content = content;
    }

    public List<Button> getButtons() {
        return buttons;
    }

    public void setButtons(List<Button> buttons) {
        this.buttons = buttons;
    }

    public List<FormComponent> getComponents() {
        return components;
    }

    public void setComponents(List<FormComponent> components) {
        this.components = components;
    }

    public void addButton(String text, String imageUrl) {
        buttons.add(new Button(text, imageUrl));
    }

    public void addComponent(FormComponent component) {
        components.add(component);
    }

    public record Button(String text, String imageUrl) {}

    public record FormComponent(
            String id,
            ComponentType type,
            String text,
            String placeholder,
            String defaultValue,
            float min,
            float max,
            float step,
            List<String> options
    ) {
        public FormComponent(String id, ComponentType type, String text) {
            this(id, type, text, "", "", 0, 100, 1, List.of());
        }
    }

    public enum ComponentType {
        LABEL,
        INPUT,
        TOGGLE,
        SLIDER,
        DROPDOWN,
        STEP_SLIDER
    }
}
