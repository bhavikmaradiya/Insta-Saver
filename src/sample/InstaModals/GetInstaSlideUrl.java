package sample.InstaModals;

import javafx.concurrent.Task;
import sample.Controller;

import java.util.ArrayList;
import java.util.List;

public class GetInstaSlideUrl extends Task<List<String>> {

    private InstaVideoModal instaVideoModal;

    public GetInstaSlideUrl(InstaVideoModal instaVideoModal) {
        this.instaVideoModal = instaVideoModal;
    }

    @Override
    protected List<String> call() throws Exception {
        List<String> urls = new ArrayList<>();
        if (!urls.isEmpty()) {
            urls.clear();
        }
        for (int i = 0; i < instaVideoModal.getGraphql().getShortcodeMedia().getEdgeSidecarToChildren().getEdges().size(); i++) {
            if (instaVideoModal.getGraphql().getShortcodeMedia().getEdgeSidecarToChildren()
                    .getEdges().get(i).getNode().getTypename().equals(Controller.INSTA_IMAGE)) {
                urls.add(instaVideoModal.getGraphql().getShortcodeMedia().getEdgeSidecarToChildren()
                        .getEdges().get(i).getNode().getDisplayResources().get(2).getSrc());
            } else if (instaVideoModal.getGraphql().getShortcodeMedia().getEdgeSidecarToChildren()
                    .getEdges().get(i).getNode().getTypename().equals(Controller.INSTA_VIDEO)) {
                urls.add(instaVideoModal.getGraphql().getShortcodeMedia().getEdgeSidecarToChildren()
                        .getEdges().get(i).getNode().getVideoUrl());
            }
        }
        return urls;
    }
}
