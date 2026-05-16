package es.jadafit.jadafit_api.dto;

import java.util.Collections;
import java.util.List;

public class ChatRequest {
    private String message;
    private List<MessageDTO> history;

    public String getMessage() { return message; }
    public void setMessage(String message) { this.message = message; }

    public List<MessageDTO> getHistory() {
        return history != null ? history : Collections.emptyList();
    }
    public void setHistory(List<MessageDTO> history) { this.history = history; }
}
