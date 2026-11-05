package com.example.mediaplayer;

import javafx.application.Application;
import javafx.application.Platform;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.scene.input.KeyCode;
import javafx.scene.input.KeyEvent;
import javafx.scene.layout.*;
import javafx.scene.media.Media;
import javafx.scene.media.MediaPlayer;
import javafx.scene.media.MediaView;
import javafx.stage.FileChooser;
import javafx.stage.Stage;
import javafx.util.Duration;

import java.io.File;
import java.util.List;

public class Main extends Application {

    private MediaPlayer mediaPlayer;
    private final ObservableList<File> playlist =
            FXCollections.observableArrayList();

    private int currentIndex = -1;

    private final ListView<File> playlistView =
            new ListView<>(playlist);

    private final MediaView mediaView =
            new MediaView();

    private final Slider progressSlider =
            new Slider(0, 100, 0);

    private final Slider volumeSlider =
            new Slider(0, 1, 0.70);

    private final Label currentTimeLabel =
            new Label("00:00");

    private final Label totalTimeLabel =
            new Label("00:00");

    private final Label titleLabel =
            new Label("No media selected");

    private final Label statusLabel =
            new Label("Ready");

    private final Label nowPlayingLabel =
            new Label("Choose a media file to begin");

    private final Button playPauseButton =
            new Button("▶");

    private final Button stopButton =
            new Button("■");

    private final Button previousButton =
            new Button("⏮");

    private final Button nextButton =
            new Button("⏭");

    private final Button muteButton =
            new Button("🔊");

    private final StackPane playerArea =
            new StackPane();

    private final VBox emptyState =
            new VBox(10);

    private final Label musicIcon =
            new Label("♫");

    private final BorderPane root =
            new BorderPane();

    private final VBox sidePanel =
            new VBox();

    private final HBox topBar =
            new HBox();

    private boolean sliderChanging = false;
    private boolean muted = false;

    private double previousVolume = 0.70;

    private int sizeMode = 1;

    @Override
    public void start(Stage stage) {

        stage.setTitle("Media Player");

        stage.setMinWidth(900);
        stage.setMinHeight(620);

        buildTopBar(stage);
        buildPlayerArea();
        buildPlaylistPanel();
        buildBottomControls();

        root.setTop(topBar);
        root.setCenter(playerArea);
        root.setRight(sidePanel);
        root.setBottom(buildControlArea());

        BorderPane.setMargin(
                playerArea,
                new Insets(18, 18, 0, 18)
        );

        BorderPane.setMargin(
                sidePanel,
                new Insets(18, 18, 0, 0)
        );

        Scene scene = new Scene(
                root,
                1250,
                760
        );


        scene.getStylesheets().add(
                getClass()
                        .getResource("/style.css").toExternalForm()
        );

        root.setFocusTraversable(true);

        installKeyboardControls(scene);

        installWindowShortcuts(stage);

        stage.setScene(scene);
        stage.show();

        Platform.runLater(() -> root.requestFocus());
    }

    private void buildTopBar(Stage stage) {

        topBar.getStyleClass().add("top-bar");

        topBar.setAlignment(Pos.CENTER_LEFT);

        topBar.setPadding(
                new Insets(15, 20, 15, 20)
        );

        topBar.setSpacing(14);

        Label logo = new Label("♫");

        logo.getStyleClass().add("logo");

        VBox brand = new VBox(1);

        Label name = new Label(
                "MEDIA PLAYER"
        );

        name.getStyleClass().add(
                "brand-name"
        );

        Label sub = new Label(
                "JavaFX Multimedia Studio"
        );

        sub.getStyleClass().add(
                "brand-subtitle"
        );

        brand.getChildren().addAll(
                name,
                sub
        );

        Region spacer = new Region();

        HBox.setHgrow(
                spacer,
                Priority.ALWAYS
        );

        Button openButton =
                new Button("+ Add Media");

        openButton.getStyleClass().add(
                "primary-button"
        );

        openButton.setOnAction(e -> {
            addFiles(stage);
            restoreKeyboardFocus();
        });

        Button themeButton =
                new Button("Theme");

        themeButton.getStyleClass().add(
                "secondary-button"
        );

        themeButton.setOnAction(e -> {
            cycleTheme();
            restoreKeyboardFocus();
        });

        Button sizeButton =
                new Button("Size");

        sizeButton.getStyleClass().add(
                "secondary-button"
        );

        sizeButton.setOnAction(e -> {
            cycleSize();
            restoreKeyboardFocus();
        });

        topBar.getChildren().addAll(
                logo,
                brand,
                spacer,
                themeButton,
                sizeButton,
                openButton
        );
    }

    private void buildPlayerArea() {

        playerArea.getStyleClass().add(
                "player-area"
        );

        StackPane videoHolder =
                new StackPane();

        videoHolder.getStyleClass().add(
                "video-holder"
        );

        mediaView.setPreserveRatio(true);

        mediaView.setFocusTraversable(false);

        mediaView.setMouseTransparent(true);

        mediaView.fitWidthProperty().bind(
                videoHolder.widthProperty()
                        .subtract(40)
        );

        mediaView.fitHeightProperty().bind(
                videoHolder.heightProperty()
                        .subtract(40)
        );

        emptyState.setAlignment(
                Pos.CENTER
        );

        musicIcon.getStyleClass().add(
                "big-music-icon"
        );

        Label emptyTitle =
                new Label(
                        "Your media starts here"
                );
        emptyTitle.getStyleClass().add(
                "empty-title"
        );

        Label emptyText =
                new Label(
                        "Add an audio or video file from your computer"
                );

        emptyText.getStyleClass().add(
                "empty-text"
        );

        emptyState.getChildren().clear();

        emptyState.getChildren().addAll(
                musicIcon,
                emptyTitle,
                emptyText
        );

        videoHolder.getChildren().addAll(
                mediaView,
                emptyState
        );

        mediaView.setVisible(false);

        playerArea.getChildren().add(
                videoHolder
        );

        StackPane.setMargin(
                videoHolder,
                new Insets(0)
        );

        nowPlayingLabel.getStyleClass().add(
                "now-playing"
        );

        StackPane.setAlignment(
                nowPlayingLabel,
                Pos.TOP_LEFT
        );

        StackPane.setMargin(
                nowPlayingLabel,
                new Insets(18)
        );

        playerArea.getChildren().add(
                nowPlayingLabel
        );
    }

    private void buildPlaylistPanel() {

        sidePanel.setPrefWidth(300);

        sidePanel.setSpacing(12);

        sidePanel.getStyleClass().add(
                "side-panel"
        );

        Label playlistTitle =
                new Label("PLAYLIST");

        playlistTitle.getStyleClass().add(
                "section-title"
        );

        Label playlistCount =
                new Label();

        playlistCount.getStyleClass().add(
                "count-label"
        );

        playlistCount.textProperty().bind(
                playlistView.itemsProperty().asString()
        );

        HBox heading =
                new HBox(
                        8,
                        playlistTitle,
                        new Region()
                );

        HBox.setHgrow(
                heading.getChildren().get(1),
                Priority.ALWAYS
        );

        playlistView.setPlaceholder(
                new Label("No media files added")
        );


        playlistView.setCellFactory(
                list -> new ListCell<File>() {

                    @Override
                    protected void updateItem(
                            File file,
                            boolean empty
                    ) {

                        super.updateItem(
                                file,
                                empty
                        );


                        if (empty || file == null) {

                            setText(null);
                            setGraphic(null);

                        } else {

                            setText(
                                    file.getName()
                            );

                            setTooltip(
                                    new Tooltip(
                                            file.getAbsolutePath()
                                    )
                            );
                        }
                    }
                }
        );

        playlistView.setOnMouseClicked(e -> {

            if (e.getClickCount() == 2) {
                playSelected();
            }

            restoreKeyboardFocus();
        });

        playlistView
                .getSelectionModel()
                .selectedItemProperty()
                .addListener(
                        (obs, oldFile, newFile) -> {

                            if (newFile != null) {

                                int index =
                                        playlist.indexOf(
                                                newFile
                                        );

                                if (index >= 0) {
                                    loadMedia(
                                            index,
                                            true
                                    );
                                }
                            }
                        }
                );


        Button add =
                new Button("+ Add");

        add.getStyleClass().add(
                "small-button"
        );

        add.setOnAction(e -> {

            addFiles(null);

            restoreKeyboardFocus();
        });

        Button remove =
                new Button("Remove");

        remove.getStyleClass().add(
                "danger-button"
        );

        remove.setOnAction(e -> {

            removeSelected();

            restoreKeyboardFocus();
        });

        HBox playlistButtons =
                new HBox(
                        8,
                        add,
                        remove
                );

        Label hint =
                new Label(
                        "Keyboard: Space play/pause • S stop • N next • P previous • ↑/↓ volume • M mute"
                );

        hint.setWrapText(true);

        hint.getStyleClass().add(
                "hint"
        );

        sidePanel.getChildren().addAll(
                heading,
                playlistView,
                playlistButtons,
                hint
        );

        VBox.setVgrow(
                playlistView,
                Priority.ALWAYS
        );
    }

    private void buildBottomControls() {
    }
    private VBox buildControlArea() {

        VBox controlArea =
                new VBox(8);

        controlArea.getStyleClass().add(
                "control-area"
        );

        controlArea.setPadding(
                new Insets(
                        14,
                        20,
                        16,
                        20
                )
        );

        HBox progressRow =
                new HBox(10);

        progressRow.setAlignment(
                Pos.CENTER
        );

        progressSlider.setMaxWidth(
                Double.MAX_VALUE
        );

        HBox.setHgrow(
                progressSlider,
                Priority.ALWAYS
        );

        currentTimeLabel.getStyleClass().add(
                "time-label"
        );

        totalTimeLabel.getStyleClass().add(
                "time-label"
        );

        progressRow.getChildren().addAll(
                currentTimeLabel,
                progressSlider,
                totalTimeLabel
        );

        HBox mainControls =
                new HBox(12);

        mainControls.setAlignment(
                Pos.CENTER
        );

        styleControl(
                previousButton,
                "control-button"
        );

        styleControl(
                playPauseButton,
                "play-button"
        );

        styleControl(
                stopButton,
                "control-button"
        );

        styleControl(
                nextButton,
                "control-button"
        );

        previousButton.setOnAction(e -> {

            previous();
            restoreKeyboardFocus();
        });

        playPauseButton.setOnAction(e -> {

            togglePlayPause();
            restoreKeyboardFocus();
        });

        stopButton.setOnAction(e -> {
            stopMedia();
            restoreKeyboardFocus();
        });

        nextButton.setOnAction(e -> {
            next();
            restoreKeyboardFocus();
        });

        mainControls.getChildren().addAll(
                previousButton,
                playPauseButton,
                stopButton,
                nextButton
        );

        HBox volumeBox =
                new HBox(8);

        volumeBox.setAlignment(
                Pos.CENTER_RIGHT
        );

        muteButton.getStyleClass().add(
                "mute-button"
        );

        muteButton.setOnAction(e -> {

            toggleMute();

            restoreKeyboardFocus();
        });

        volumeSlider.setPrefWidth(120);

        volumeSlider.valueProperty().addListener(
                (obs, oldValue, newValue) -> {

                    if (mediaPlayer != null) {

                        mediaPlayer.setVolume(
                                newValue.doubleValue()
                        );
                    }

                    if (newValue.doubleValue() > 0) {

                        previousVolume =
                                newValue.doubleValue();

                        muted = false;

                        muteButton.setText(
                                "🔊"
                        );
                    }
                }
        );

        Label volumeLabel =
                new Label("Volume");

        volumeLabel.getStyleClass().add(
                "volume-label"
        );

        volumeBox.getChildren().addAll(
                volumeLabel,
                volumeSlider,
                muteButton
        );

        HBox lowerRow =
                new HBox();

        lowerRow.setAlignment(
                Pos.CENTER
        );

        Region lowerSpacer =
                new Region();

        lowerRow.getChildren().addAll(
                mainControls,
                lowerSpacer,
                volumeBox
        );

        HBox.setHgrow(
                lowerSpacer,
                Priority.ALWAYS
        );

        statusLabel.getStyleClass().add(
                "status-label"
        );

        controlArea.getChildren().addAll(
                progressRow,
                lowerRow,
                statusLabel
        );

        progressSlider
                .valueChangingProperty()
                .addListener(
                        (obs, old, changing) ->
                                sliderChanging = changing
                );

        progressSlider.setOnMouseReleased(
                e -> {

                    seekFromSlider();

                    restoreKeyboardFocus();
                }
        );

        return controlArea;
    }

    private void styleControl(
            Button button,
            String style
    ) {

        button.getStyleClass().add(style);
    }

    private void addFiles(Stage stage) {

        FileChooser chooser =
                new FileChooser();

        chooser.setTitle(
                "Select Audio / Video Files"
        );

        chooser.getExtensionFilters().add(
                new FileChooser.ExtensionFilter(
                        "Media Files",
                        "*.mp3",
                        "*.wav",
                        "*.m4a",
                        "*.aac",
                        "*.mov",
                        "*.aiff",
                        "*.mp4",
                        "*.m4v",
                        "*.fxm",
                        "*.flv",
                        "*.webm"
                )
        );


        List<File> files =
                chooser.showOpenMultipleDialog(
                        stage != null
                                ? stage
                                : playlistView
                                .getScene()
                                .getWindow()
                );

        if (files != null) {

            for (File file : files) {

                if (!playlist.contains(file)) {

                    playlist.add(file);
                }
            }

            if (currentIndex == -1
                    && !playlist.isEmpty()) {

                playlistView
                        .getSelectionModel()
                        .select(0);
            }
        }
    }

    private void removeSelected() {
        int selected =
                playlistView
                        .getSelectionModel()
                        .getSelectedIndex();

        if (selected < 0) {

            statusLabel.setText(
                    "Select a playlist item to remove."
            );

            return;
        }

        boolean removingCurrent =
                selected == currentIndex;

        playlist.remove(selected);

        if (playlist.isEmpty()) {

            currentIndex = -1;

            releasePlayer();

            titleLabel.setText(
                    "No media selected"
            );

            nowPlayingLabel.setText(
                    "Choose a media file to begin"
            );

            statusLabel.setText(
                    "Playlist is empty"
            );

            return;
        }

        if (selected < currentIndex) {
            currentIndex--;
        }

        if (removingCurrent) {

            int nextIndex =
                    Math.min(
                            selected,
                            playlist.size() - 1
                    );

            loadMedia(
                    nextIndex,
                    false
            );
        } else {
            playlistView
                    .getSelectionModel()
                    .select(
                            Math.min(
                                    selected,
                                    playlist.size() - 1
                            )
                    );
        }
    }
    private void playSelected() {
        int selected =
                playlistView
                        .getSelectionModel()
                        .getSelectedIndex();

        if (selected >= 0) {

            if (selected != currentIndex) {

                loadMedia(
                        selected,
                        true
                );

            } else {

                togglePlayPause();
            }
        }
    }
    private void loadMedia(
            int index,
            boolean autoPlay
    ) {

        if (index < 0
                || index >= playlist.size()) {

            return;
        }

        currentIndex = index;

        File file =
                playlist.get(index);

        releasePlayer();

        try {

            Media media =
                    new Media(
                            file.toURI().toString()
                    );

            mediaPlayer =
                    new MediaPlayer(media);

            mediaView.setMediaPlayer(
                    mediaPlayer
            );

            boolean video =
                    isVideoFile(file);

            if (video) {
                mediaView.setVisible(true);
                mediaView.setManaged(true);
                emptyState.setVisible(false);
                emptyState.setManaged(false);

                nowPlayingLabel.setVisible(false);
                nowPlayingLabel.setManaged(false);

            }
            else {
                mediaView.setVisible(false);
                mediaView.setManaged(false);

                emptyState.setVisible(true);
                emptyState.setManaged(true);

                emptyState.getChildren().clear();

                emptyState.getChildren().add(
                        musicIcon
                );

                musicIcon.setVisible(true);
                musicIcon.setManaged(true);

                nowPlayingLabel.setVisible(false);
                nowPlayingLabel.setManaged(false);
            }

            titleLabel.setText(
                    file.getName()
            );

            nowPlayingLabel.setText(
                    "NOW PLAYING  •  "
                            + file.getName()
            );

            statusLabel.setText(
                    "Loading: "
                            + file.getName()
            );

            mediaPlayer.setVolume(
                    muted
                            ? 0
                            : volumeSlider.getValue()
            );

            mediaPlayer.setOnReady(() -> {

                Duration total =
                        media.getDuration();

                progressSlider.setDisable(
                        false
                );

                totalTimeLabel.setText(
                        formatTime(total)
                );

                statusLabel.setText(
                        "Ready • "
                                + formatTime(total)
                );

                if (autoPlay) {

                    mediaPlayer.play();

                    playPauseButton.setText(
                            "❚❚"
                    );
                }

                restoreKeyboardFocus();
            });

            mediaPlayer
                    .currentTimeProperty()
                    .addListener(
                            (obs,
                             oldTime,
                             newTime) -> {

                                if (!sliderChanging
                                        && mediaPlayer != null) {

                                    Duration total =
                                            mediaPlayer
                                                    .getTotalDuration();

                                    if (total != null
                                            && total.greaterThan(
                                            Duration.ZERO)) {

                                        progressSlider.setValue(
                                                newTime.toMillis()
                                                        / total.toMillis()
                                                        * 100.0
                                        );
                                    }
                                }

                                currentTimeLabel.setText(
                                        formatTime(newTime)
                                );
                            }
                    );

            mediaPlayer.setOnPlaying(() -> {

                playPauseButton.setText(
                        "❚❚"
                );

                statusLabel.setText(
                        "Playing"
                );

                restoreKeyboardFocus();
            });

            mediaPlayer.setOnPaused(() -> {

                playPauseButton.setText(
                        "▶"
                );

                statusLabel.setText(
                        "Paused"
                );

                restoreKeyboardFocus();
            });

            mediaPlayer.setOnStopped(() -> {

                playPauseButton.setText(
                        "▶"
                );

                statusLabel.setText(
                        "Stopped"
                );

                restoreKeyboardFocus();
            });

            mediaPlayer.setOnEndOfMedia(() -> {

                playPauseButton.setText(
                        "▶"
                );

                statusLabel.setText(
                        "Finished"
                );

                if (currentIndex
                        < playlist.size() - 1) {

                    next();

                } else {

                    restoreKeyboardFocus();
                }
            });

            mediaPlayer.setOnError(() -> {

                String error =
                        mediaPlayer.getError() != null
                                ? mediaPlayer
                                .getError()
                                .getMessage()
                                : "Unable to play this file.";

                statusLabel.setText(
                        "Error: "
                                + error
                );

                restoreKeyboardFocus();
            });

            playlistView
                    .getSelectionModel()
                    .select(index);

            restoreKeyboardFocus();

        } catch (Exception ex) {

            statusLabel.setText(
                    "Could not load: "
                            + file.getName()
            );

            ex.printStackTrace();
        }
    }

    private boolean isVideoFile(File file) {

        String name =
                file.getName().toLowerCase();

        return name.endsWith(".mp4")
                || name.endsWith(".m4v")
                || name.endsWith(".fxm")
                || name.endsWith(".flv")
                || name.endsWith(".mov")
                || name.endsWith(".webm");
    }

    private void togglePlayPause() {

        if (mediaPlayer == null) {

            if (!playlist.isEmpty()) {

                loadMedia(
                        0,
                        true
                );
            }
            return;
        }

        switch (mediaPlayer.getStatus()) {

            case PLAYING:

                mediaPlayer.pause();

                break;

            case PAUSED:

            case READY:

            case STOPPED:

                mediaPlayer.play();

                break;

            default:

                break;
        }
    }

    private void stopMedia() {

        if (mediaPlayer != null) {

            mediaPlayer.stop();

            progressSlider.setValue(0);

            currentTimeLabel.setText(
                    "00:00"
            );
        }
    }

    private void next() {

        if (playlist.isEmpty()) {
            return;
        }

        int nextIndex =
                currentIndex + 1;

        if (nextIndex >= playlist.size()) {

            nextIndex = 0;
        }

        loadMedia(
                nextIndex,
                true
        );
    }

    private void previous() {

        if (playlist.isEmpty()) {
            return;
        }

        int previousIndex =
                currentIndex - 1;

        if (previousIndex < 0) {

            previousIndex =
                    playlist.size() - 1;
        }

        loadMedia(
                previousIndex,
                true
        );
    }

    private void toggleMute() {

        if (mediaPlayer == null) {
            return;
        }

        if (!muted) {

            previousVolume =
                    volumeSlider.getValue();

            volumeSlider.setValue(0);

            mediaPlayer.setVolume(0);

            muteButton.setText(
                    "🔇"
            );

            muted = true;

        } else {

            double restore =
                    previousVolume <= 0
                            ? 0.70
                            : previousVolume;

            volumeSlider.setValue(
                    restore
            );

            mediaPlayer.setVolume(
                    restore
            );

            muteButton.setText(
                    "🔊"
            );

            muted = false;
        }
    }

    private void seekFromSlider() {

        if (mediaPlayer == null) {
            return;
        }

        Duration total =
                mediaPlayer.getTotalDuration();

        if (total != null
                && total.greaterThan(
                Duration.ZERO)) {

            double percentage =
                    progressSlider.getValue()
                            / 100.0;


            mediaPlayer.seek(
                    total.multiply(
                            percentage
                    )
            );
        }
    }

    private String formatTime(
            Duration duration
    ) {

        if (duration == null
                || duration.isUnknown()) {

            return "00:00";
        }

        int totalSeconds =
                (int) Math.floor(
                        duration.toSeconds()
                );

        int hours =
                totalSeconds / 3600;

        int minutes =
                (totalSeconds % 3600) / 60;

        int seconds =
                totalSeconds % 60;

        if (hours > 0) {

            return String.format(
                    "%02d:%02d:%02d",
                    hours,
                    minutes,
                    seconds
            );
        }

        return String.format(
                "%02d:%02d",
                minutes,
                seconds
        );
    }

    private void releasePlayer() {

        if (mediaPlayer != null) {

            mediaPlayer.stop();

            mediaPlayer.dispose();

            mediaPlayer = null;

            mediaView.setMediaPlayer(
                    null
            );

            mediaView.setVisible(
                    false
            );
        }
    }

    private void installKeyboardControls(
            Scene scene
    ) {

        scene.addEventFilter(
                KeyEvent.KEY_PRESSED,
                event -> {

                    KeyCode key =
                            event.getCode();

                    switch (key) {

                        case SPACE:

                            togglePlayPause();

                            event.consume();

                            break;

                        case S:

                            stopMedia();

                            event.consume();

                            break;

                        case N:

                            next();

                            event.consume();

                            break;

                        case P:

                            previous();

                            event.consume();

                            break;

                        case UP:

                            changeVolume(
                                    0.10
                            );

                            event.consume();

                            break;

                        case DOWN:

                            changeVolume(
                                    -0.10
                            );

                            event.consume();

                            break;

                        case M:

                            toggleMute();

                            event.consume();

                            break;

                        default:

                            break;
                    }
                }
        );
    }

    private void changeVolume(
            double amount
    ) {
        double value =
                Math.max(
                        0,
                        Math.min(
                                1,
                                volumeSlider.getValue()
                                        + amount
                        )
                );

        volumeSlider.setValue(
                value
        );

        if (mediaPlayer != null) {

            mediaPlayer.setVolume(
                    value
            );
        }

        if (value > 0) {

            muted = false;

            muteButton.setText(
                    "🔊"
            );

        } else {

            muted = true;

            muteButton.setText(
                    "🔇"
            );
        }
    }

    private void restoreKeyboardFocus() {

        Platform.runLater(
                () -> {

                    if (root != null) {

                        root.requestFocus();
                    }
                }
        );
    }

    private void installWindowShortcuts(
            Stage stage
    ) {

        stage.setOnCloseRequest(
                e -> releasePlayer()
        );
    }

    private void cycleTheme() {

        Scene scene =
                root.getScene();

        if (scene == null) {
            return;
        }

        ObservableList<String> stylesheets =
                scene.getStylesheets();

        String css =
                getClass()
                        .getResource("/style.css")
                        .toExternalForm();

        String[] themes = {
                "default-theme",
                "blue-theme",
                "purple-theme",
                "dark-theme"
        };

        int current = 0;

        for (int i = 0;
             i < themes.length;
             i++) {

            if (root.getStyleClass()
                    .contains(themes[i])) {

                current = i;

                break;
            }
        }

        root.getStyleClass().removeAll(
                themes
        );

        root.getStyleClass().add(
                themes[
                        (current + 1)
                                % themes.length
                        ]
        );

        if (!stylesheets.contains(css)) {

            stylesheets.add(css);
        }
    }

    private void cycleSize() {

        root.getStyleClass().removeAll(
                "size-small",
                "size-normal",
                "size-large"
        );

        sizeMode =
                (sizeMode + 1) % 3;

        if (sizeMode == 0) {

            root.getStyleClass().add(
                    "size-small"
            );

        } else if (sizeMode == 1) {

            root.getStyleClass().add(
                    "size-normal"
            );

        } else {

            root.getStyleClass().add(
                    "size-large"
            );
        }
    }

    @Override
    public void stop() {

        releasePlayer();
    }

    public static void main(
            String[] args
    ) {

        launch(args);
    }
}