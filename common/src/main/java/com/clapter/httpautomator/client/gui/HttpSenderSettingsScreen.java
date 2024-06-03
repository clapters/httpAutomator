package com.clapter.httpautomator.client.gui;

import com.clapter.httpautomator.Constants;
import com.clapter.httpautomator.blockentity.HttpReceiverBlockEntity;
import com.clapter.httpautomator.blockentity.HttpSenderBlockEntity;
import com.clapter.httpautomator.client.gui.widgets.ScrollableWidget;
import com.clapter.httpautomator.network.packet.SUpdateHttpReceiverValuesPacket;
import com.clapter.httpautomator.network.packet.SUpdateHttpSenderValuesPacket;
import com.clapter.httpautomator.platform.Services;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.components.Button;
import net.minecraft.client.gui.components.EditBox;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.network.chat.Component;
import org.jetbrains.annotations.NotNull;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class HttpSenderSettingsScreen extends Screen {

    private static Component TITLE = Component.translatable("gui."+ Constants.MOD_ID + ".http_sender_settings_screen");
    private static Component START_TEXT = Component.translatable("gui."+ Constants.MOD_ID + ".http_sender_startbutton");

    private final int screenWidth;
    private final int screenHeight;
    private int leftPos;
    private int topPos;
    private HttpSenderBlockEntity blockEntity;

    private boolean forceMapInit;
    private Button startButton;
    private EditBox endpoint;
    private ScrollableWidget scrollablePanel;
    private String countInputAsString = "";
    private Button saveCountButton;
    private EditBox countInput;
    private final List<EditBox> parameterFields = new ArrayList<>();
    private final List<EditBox> parameterValuesFields = new ArrayList<>();
    private int numberOfFields = 0;

    private String endpointText;


    public HttpSenderSettingsScreen(HttpSenderBlockEntity blockEntity) {
        super(TITLE);
        screenWidth = 176;
        screenHeight = 166;
        this.blockEntity = blockEntity;
        forceMapInit = true;
    }

    private void readParameterMap() {
        Map<String, String> parameterMap = this.blockEntity.getValues().parameterMap;
        if(!parameterMap.isEmpty()){
            for(Map.Entry<String, String> entry : parameterMap.entrySet()){
                //System.out.println(entry.getKey() + ": " + entry.getValue());
                EditBox parBox = new EditBox(this.font, leftPos+90, topPos + 100, 50, 20, Component.literal("Parameter"));
                EditBox valBox = new EditBox(this.font, leftPos +180, topPos + 100, 50, 20, Component.literal("Value"));
                this.parameterFields.add(parBox);
                this.parameterFields.add(valBox);
                parBox.setValue(entry.getKey());
                valBox.setValue(entry.getValue());

            }
        }
    }

    @Override
    protected void init() {
        super.init();
        this.leftPos = (this.width - screenWidth) / 2;
        this.topPos = (this.height - screenHeight) / 2;
        this.startButton = addRenderableWidget(startButton.builder(
                        START_TEXT, this::handleStartButton)
                .bounds(leftPos+74, topPos+200, 50, 20)
                .build()
        );
        if(this.forceMapInit) {
            this.readParameterMap();
            this.forceMapInit = false;
        }
        this.endpoint = new EditBox(font, leftPos + 50, topPos + 6, 198, 20, Component.empty());
        this.endpoint.setResponder(text -> {
            endpointText = text;
        });
        endpoint.insertText(blockEntity.getValues().url);
        addRenderableWidget(endpoint);

        countInput = new EditBox(this.font, leftPos + 50, topPos + 30, 100, 20, Component.literal("Count"));
        this.countInput.setResponder(text -> {
            countInputAsString = text;
        });
        this.addRenderableWidget(countInput);
        this.saveCountButton = this.addRenderableWidget(saveCountButton.builder(
                Component.literal("Set Count"),button -> {
                    if(countInputAsString.isEmpty())return;
                    numberOfFields = Integer.parseInt(countInput.getValue());
                    createInputFields(numberOfFields);
                }).bounds(leftPos + 150, topPos + 30, 100, 20).build());

        if(!this.parameterFields.isEmpty()) {
            this.drawParameters();
        }
    }

    private void drawParameters(){
        this.scrollablePanel = new ScrollableWidget(leftPos + 50, topPos + 60, 100, 100, this.parameterFields);
        this.addRenderableWidget(scrollablePanel);
    }

    private void createInputFields(int numberOfFields) {
        List<EditBox> containigBox = new ArrayList<>(this.parameterFields);
        this.parameterFields.clear();
        for(int i = 0; i < numberOfFields; i++){
            EditBox parBox = new EditBox(this.font, leftPos+90, topPos + 100, 50, 20, Component.literal("Parameter"));
            EditBox valBox = new EditBox(this.font, leftPos +180, topPos + 100, 50, 20, Component.literal("Value"));
            this.parameterFields.add(parBox);
            this.parameterFields.add(valBox);
            if(containigBox.size()/2 > i){
                parBox.setValue(containigBox.get(i).getValue());
                valBox.setValue(containigBox.get(i+1).getValue());
            }
        }
        this.clearWidgets();
        this.init();
    }

    private void handleStartButton(Button button){
        if(this.checkValues()){
            //SEND UPDATE PACKET TO SERVER
            HttpSenderBlockEntity.Values values = blockEntity.getValues();
            values.url = this.endpointText;
            if(this.scrollablePanel != null){
                values.parameterMap = this.getParameterValues();
            }
            Services.PACKET_HANDLER.sendPacketToServer(new SUpdateHttpSenderValuesPacket(
                    this.blockEntity.getBlockPos(),
                    values));
        }
    }

    private Map<String, String> getParameterValues(){
        return getParameterValuesFromScrollable(this.scrollablePanel);
    }

    @NotNull
    static Map<String, String> getParameterValuesFromScrollable(ScrollableWidget scrollablePanel) {
        List<EditBox> input = scrollablePanel.getInputBoxes();
        Map<String, String> parameterMap = new HashMap<String, String>();
        for(int i = 0; i < input.size(); i+=2){
            if(!input.get(i).getValue().isEmpty() && !input.get(i+1).getValue().isEmpty()){
                parameterMap.put(input.get(i).getValue(), input.get(i+1).getValue());
            }
        }
        return parameterMap;
    }

    private boolean checkValues(){
        return endpointText != null && !endpointText.isEmpty();
    }

    @Override
    public void render(GuiGraphics $$0, int $$1, int $$2, float $$3) {
        super.render($$0, $$1, $$2, $$3);
    }

    @Override
    public boolean isPauseScreen() {
        return false;
    }
}
