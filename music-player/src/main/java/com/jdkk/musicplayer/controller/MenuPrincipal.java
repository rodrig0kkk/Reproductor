package com.jdkk.musicplayer.controller;

import javafx.beans.property.BooleanProperty;
import javafx.beans.property.SimpleBooleanProperty;
import javafx.collections.ListChangeListener;
import javafx.geometry.Bounds;
import javafx.scene.control.*;
import javafx.application.Application;
import javafx.application.Platform;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.Scene;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.layout.*;
import javafx.scene.media.MediaPlayer;
import javafx.scene.paint.Color;
import javafx.stage.Modality;
import javafx.stage.Stage;
import javafx.stage.Popup;
import javafx.util.Duration;

import java.util.logging.Level;
import java.util.logging.Logger;


import java.io.File;
import java.util.*;

import org.kordamp.ikonli.fontawesome5.FontAwesomeSolid;
import org.kordamp.ikonli.javafx.FontIcon;

import com.jdkk.musicplayer.controller.Utiles;
import com.jdkk.musicplayer.model.Biblioteca;
import com.jdkk.musicplayer.model.BuscarCancion;
import com.jdkk.musicplayer.model.Cancion;
import com.jdkk.musicplayer.model.ColaDeReproduccion;
import com.jdkk.musicplayer.model.ListaDeReproduccion;
import com.jdkk.musicplayer.model.Playlist;
import com.jdkk.musicplayer.view.AlbumCoverView;
import com.jdkk.musicplayer.view.ButtonPlayer;

public class MenuPrincipal extends Application {
    private static final String MUSIC_DIRECTORY = System.getProperty("user.home") + "/Music/";
    private final Scanner scanner = new Scanner(System.in);
    private final Cancion reproductor;
    private final Biblioteca biblioteca;
    private final ColaDeReproduccion colaDeReproduccion;
    private final BuscarCancion buscador;
    private final List<ButtonPlayer> listButtonPlayers;
    private final ProgressBar progressBar;
    private final double BUTTON_WIDTH = 150;
    private AlbumCoverView albumCover;

    public MenuPrincipal() {
        System.out.println("Music directory" + MUSIC_DIRECTORY);
        listButtonPlayers = new ArrayList<>();
        initUIButtons();
        this.biblioteca = new Biblioteca();
        biblioteca.cargarListaCancionesJson("lista_canciones.json");
        progressBar = new ProgressBar(0);
        this.albumCover = new AlbumCoverView(); // Initialize here for access
        this.reproductor = new Cancion(biblioteca.getListaCanciones(), listButtonPlayers, progressBar, albumCover);
        this.colaDeReproduccion = new ColaDeReproduccion(reproductor, scanner, biblioteca);
        this.buscador = new BuscarCancion(biblioteca, reproductor);
    }

    private void initUIButtons() {
        ButtonPlayer pausaButton = new ButtonPlayer(16, 16, "/icons/play.png");
        ButtonPlayer nextButton = new ButtonPlayer(16, 16, "/icons/next.png");
        ButtonPlayer previousButton = new ButtonPlayer(16, 16, "/icons/previous.png");
        ButtonPlayer onRepeatButton = new ButtonPlayer(16, 16, "/icons/repeat.png");
        ButtonPlayer shuffleButton = new ButtonPlayer(16, 16, "/icons/arrows-shuffle.png");
        ButtonPlayer volumenButton = new ButtonPlayer(16, 16, "/icons/volume.png");

        listButtonPlayers.add(pausaButton);
        listButtonPlayers.add(nextButton);
        listButtonPlayers.add(previousButton);
        listButtonPlayers.add(onRepeatButton);
        listButtonPlayers.add(shuffleButton);
        listButtonPlayers.add(volumenButton);
    }

    @Override
    public void start(Stage primaryStage) {
        try {
            System.out.println("Iniciando la interfaz...");
            primaryStage.setTitle("Softcorp Music Player");
            Image icon = new Image(getClass().getResourceAsStream("/images/icono.png"));
            primaryStage.getIcons().add(icon);

            Slider timelineSlider = new Slider();
            timelineSlider.setMin(0);
            timelineSlider.setMax(100);
            timelineSlider.setValue(0);
            timelineSlider.setPrefHeight(16);
            HBox.setHgrow(timelineSlider, Priority.ALWAYS);
            timelineSlider.setPickOnBounds(false);

            progressBar.setProgress(0);
            progressBar.getStyleClass().add("progress-bar");
            progressBar.setMaxWidth(Double.MAX_VALUE);
            progressBar.setPrefHeight(4);

            progressBar.setMaxWidth(Double.MAX_VALUE);
            timelineSlider.setMaxWidth(Double.MAX_VALUE);
            StackPane sliderStack = new StackPane(progressBar, timelineSlider);

            Label timestampLabel = new Label("00:00 / 00:00");
            timestampLabel.setStyle("-fx-text-fill: white; -fx-font-size: 12px;");

            reproductor.setTimelineSlider(timelineSlider);
            reproductor.setTimestampLabel(timestampLabel);

            HBox.setHgrow(timelineSlider, Priority.ALWAYS);
            HBox.setHgrow(sliderStack, Priority.ALWAYS);
            HBox sliderWithTime = new HBox(10, sliderStack, timestampLabel);
            HBox.setMargin(sliderWithTime, new Insets(0, 5, 0, 5));
            sliderWithTime.setAlignment(Pos.CENTER);

            Map<Integer, Integer> indicesMap = new HashMap<>();
            ListView<String> listView = new ListView<>();
            ObservableList<String> cancionesObservableList = FXCollections.observableArrayList();
            listView.setItems(cancionesObservableList);
            listView.getStyleClass().add("list-view");
            BorderPane.setMargin(listView, new Insets(10));

            cargarCancionesPorTitulo(cancionesObservableList, indicesMap);

            listView.setCellFactory(lv -> new ListCell<>() {
                @Override
                protected void updateItem(String item, boolean empty) {
                    super.updateItem(item, empty);
                    if (empty || item == null) {
                        setText(null);
                        setGraphic(null);
                    } else {
                        HBox cellBox = new HBox(10);
                        cellBox.setAlignment(Pos.CENTER_LEFT);

                        Label songLabel = new Label(item);
                        songLabel.setStyle("-fx-text-fill: white;");

                        Button addToQueueButton = new Button();
                        try {
                            ImageView icon = new ImageView(new Image(getClass().getResourceAsStream("/icons/agregarCola.png")));
                            icon.setFitHeight(16);
                            icon.setFitWidth(16);
                            addToQueueButton.setGraphic(icon);
                        } catch (Exception e) {
                            Logger.getLogger("MenuPrincipal").log(Level.SEVERE, "Error al cargar el ícono agregarCola.png", e);
                            addToQueueButton.setText("+");
                        }
                        addToQueueButton.getStyleClass().add("button");

                        addToQueueButton.setOnAction(event -> {
                            int selectedIndex = getIndex();
                            Integer originalIndex = indicesMap.get(selectedIndex);
                            if (originalIndex != null && originalIndex != -1) {
                                File selectedSong = biblioteca.getListaCanciones().get(originalIndex);
                                List<File> queue = reproductor.getListaActual();
                                queue.add(selectedSong);
                           }
                        });

                        if (indicesMap.get(getIndex()) != null && indicesMap.get(getIndex()) != -1) {
                            cellBox.getChildren().setAll(songLabel, addToQueueButton);
                        } else {
                            cellBox.getChildren().setAll(songLabel);
                        }

                        setGraphic(cellBox);
                        setText(null);
                    }
                }
            });

            Button verCola = new Button();
            try {
                Image iconoVerCola = new Image(getClass().getResourceAsStream("/icons/verCola.png"));
                ImageView vistaIconoVerCola = new ImageView(iconoVerCola);
                vistaIconoVerCola.setPreserveRatio(true);
                vistaIconoVerCola.setFitHeight(20);
                verCola.setGraphic(vistaIconoVerCola);
                verCola.setStyle("-fx-background-color: transparent; -fx-padding: 5px; -fx-border-width: 0;");

            } catch (Exception e) {
                System.err.println("Error al cargar el icono de Ver Cola: " + e.getMessage());
            }

            Button btnEliminar = new Button();
            try {
                Image iconoEliminar = new Image(getClass().getResourceAsStream("/icons/eliminar.png"));
                ImageView vistaIconoEliminar = new ImageView(iconoEliminar);
                vistaIconoEliminar.setPreserveRatio(true);
                vistaIconoEliminar.setFitHeight(22);
                btnEliminar.setGraphic(vistaIconoEliminar);
                btnEliminar.setStyle("-fx-background-color: #4a4a4a; -fx-padding: 5px;");
                btnEliminar.setOnAction(event -> {
                    // Lógica para manejar la acción de eliminar
                    System.out.println("Botón de eliminar presionado");
                });
            } catch (Exception e) {
                System.err.println("Error al cargar el icono de eliminar: " + e.getMessage());
            }

           Button musicas = new Button("Músicas");
            Button artista = new Button("Artista");
            Button genero = new Button("Género");
            Button titulo = new Button("Título");

            double buttonWidth1 = 90;
            double buttonWidth = 85;
  
             musicas.setMinWidth(100);
            artista.setPrefWidth(buttonWidth);
            genero.setPrefWidth(buttonWidth1);
            titulo.setPrefWidth(buttonWidth);

            HBox.setHgrow(musicas, Priority.NEVER);
            HBox.setHgrow(artista, Priority.NEVER);
            HBox.setHgrow(genero, Priority.NEVER);
            HBox.setHgrow(titulo, Priority.NEVER);

            musicas.getStyleClass().add("button");
            artista.getStyleClass().add("button");
            genero.getStyleClass().add("button");
            titulo.getStyleClass().add("button");
            musicas.setPrefWidth(90);

            FontIcon IcoEliminar = new FontIcon(FontAwesomeSolid.TRASH);
            IcoEliminar.setIconSize(16);
            IcoEliminar.setIconColor(Color.WHITE);
            IcoEliminar.getStyleClass().add("icono-eliminar");

            Button eliminarBtn = new Button("", IcoEliminar);
            HBox.setHgrow(eliminarBtn, Priority.NEVER);
            eliminarBtn.getStyleClass().add("button");

            HBox botonesIzquierda = new HBox(10, musicas, artista, genero, titulo, btnEliminar);
            botonesIzquierda.setAlignment(Pos.CENTER);

            VBox panelIzquierdo = new VBox(10, botonesIzquierda, listView);
            panelIzquierdo.getStyleClass().add("panel-izquierdo");
            panelIzquierdo.setMinWidth(450);
             panelIzquierdo.setMinHeight(10);
            VBox.setVgrow(listView, Priority.ALWAYS);


            btnEliminar.setOnAction(e -> {
                int indiceSeleccionado = listView.getSelectionModel().getSelectedIndex();
                albumCover.updateCover(reproductor.getMediaPlayer());

                if (indiceSeleccionado >= 0) {
                    biblioteca.eliminarCancion(indiceSeleccionado);
                    biblioteca.guardarListaCancionesJson("lista_canciones.json");
                    biblioteca.cargarListaCancionesJson("lista_canciones.json");
                    cargarCancionesPorTitulo(cancionesObservableList, indicesMap);
                   
                }
            });
           
            Button pausa = listButtonPlayers.getFirst().getButtonPlayer();
            Button siguiente = listButtonPlayers.get(1).getButtonPlayer();
            Button anterior = listButtonPlayers.get(2).getButtonPlayer();
            Button onRepeat = listButtonPlayers.get(3).getButtonPlayer();
            Button shuffle = listButtonPlayers.get(4).getButtonPlayer();

            Button volume = listButtonPlayers.get(5).getButtonPlayer();
            Slider volumeSlider = new Slider(0, 1, 0.8);
            Label volumeLabel = new Label("80%");
            volumeLabel.getStyleClass().add("volume-label");

            volumeSlider.setPrefWidth(120);
            volumeSlider.getStyleClass().add("volume-slider");
            volumeSlider.setShowTickMarks(false);
            volumeSlider.setShowTickLabels(false);

            volumeSlider.valueProperty().addListener((obs, oldVal, newVal) -> {
                reproductor.setVolumeCancion(volumeSlider.getValue());
                int percent = (int) Math.round(newVal.doubleValue() * 100);
                volumeLabel.setText(percent + "%");
            });
            volumeSlider.setOnMousePressed(event -> {
                if (!volumeSlider.isDisabled()) {
                    double mouseX = event.getX();
                    double width = volumeSlider.getWidth();
                    double min = volumeSlider.getMin();
                    double max = volumeSlider.getMax();

                    double percent = mouseX / width;
                    double newValue = min + percent * (max - min);
                    volumeSlider.setValue(newValue);
                }
            });

            Popup sliderPopup = new Popup();
            HBox popupContent = new HBox(5, volumeLabel, volumeSlider);
            popupContent.getStyleClass().add("popup-volume");
            sliderPopup.getContent().add(popupContent);
            sliderPopup.setAutoHide(true);

            volume.setOnAction(e -> {
                if (sliderPopup.isShowing()) {
                    sliderPopup.hide();
                } else {
                    Bounds bounds = volume.localToScreen(volume.getBoundsInLocal());
                    sliderPopup.show(volume, bounds.getMinX(), bounds.getMinY() - popupContent.getHeight() - 5);
                }
            });

            onRepeat.getStyleClass().add("button");
            pausa.getStyleClass().add("button");
            siguiente.getStyleClass().add("button");
            anterior.getStyleClass().add("button");
            shuffle.getStyleClass().add("button");
            verCola.getStyleClass().add("button");

            HBox botonesControl = new HBox(15, verCola, onRepeat, anterior, pausa, siguiente, shuffle, volume);
            botonesControl.setAlignment(Pos.CENTER);
            botonesControl.getStyleClass().add("botones-control");

            BorderPane.setMargin(albumCover.getView(), new Insets(10));
            VBox.setVgrow(albumCover.getView(), Priority.ALWAYS);

            VBox centerPanel = new VBox(10, albumCover.getView(), sliderWithTime, botonesControl);
            VBox.setMargin(sliderWithTime, new Insets(0, 15, 0, 15));
            centerPanel.setAlignment(Pos.CENTER);
            VBox.setVgrow(centerPanel, Priority.ALWAYS);

            HBox panelSuperior = new HBox(5);
            panelSuperior.getStyleClass().add("panel-superior");
            panelSuperior.setAlignment(Pos.CENTER);


            TextField buscarField = new TextField();
            buscarField.setPromptText("Buscar por título, artista, género o álbum...");
            buscarField.getStyleClass().add("text-field");
            buscarField.setPrefHeight(35);

            ComboBox<String> historialBusquedas = new ComboBox<>();
            historialBusquedas.setPromptText("Historial de búsquedas");
            historialBusquedas.getStyleClass().add("combo-box");
            historialBusquedas.setPrefHeight(35);
            actualizarHistorial(historialBusquedas);

            historialBusquedas.setOnMouseClicked(event -> historialBusquedas.show());

            buscarField.setOnAction(event -> {
                String query = buscarField.getText().trim().toLowerCase();
                if (!query.isEmpty()) {
                    buscador.getHistorialBusquedas().remove(query);
                    buscador.agregarHistorial(query);
                    actualizarHistorial(historialBusquedas);
                    List<File> resultados = buscador.buscarMetadatos(query);
                    if (!resultados.isEmpty()) {
                      
                        mostrarResultadosBusqueda(resultados, cancionesObservableList, indicesMap);
                    }
                } else {
                    cargarCancionesPorTitulo(cancionesObservableList, indicesMap);
                }
            });

            historialBusquedas.setOnAction(event -> {
                String selected = historialBusquedas.getSelectionModel().getSelectedItem();
                if (selected != null) {
                    buscador.getHistorialBusquedas().remove(selected);
                    buscador.agregarHistorial(selected);
                    actualizarHistorial(historialBusquedas);
                    buscarField.setText(selected);
                    List<File> resultados = buscador.buscarMetadatos(selected);
                    if (!resultados.isEmpty()) {
                      
                        mostrarResultadosBusqueda(resultados, cancionesObservableList, indicesMap);
                    }
                }
            });

            HBox barraBusqueda = new HBox(5, buscarField, historialBusquedas);
            barraBusqueda.getStyleClass().add("barra-busqueda");
            HBox.setHgrow(buscarField, Priority.ALWAYS);
            HBox.setHgrow(historialBusquedas, Priority.ALWAYS);

            MenuButton menuAnadirCancion = new MenuButton("Añadir Canción");
            MenuItem itemArchivos = new MenuItem("Sel.. archivos");
            MenuItem itemCarpeta = new MenuItem("Sel.. carpeta");

            menuAnadirCancion.getItems().addAll(itemArchivos, itemCarpeta);
            menuAnadirCancion.getStyleClass().add("combo-box");
            menuAnadirCancion.setPrefHeight(35);

            itemArchivos.setOnAction(e -> {
                biblioteca.agregarCanciones(new Stage());
                biblioteca.guardarListaCancionesJson("lista_canciones.json");
                biblioteca.cargarListaCancionesJson("lista_canciones.json");
                cargarCancionesPorTitulo(cancionesObservableList, indicesMap);
             
            });

            itemCarpeta.setOnAction(e -> {
                biblioteca.agregarCancionDeCarpeta(new Stage());
                biblioteca.guardarListaCancionesJson("lista_canciones.json");
                biblioteca.cargarListaCancionesJson("lista_canciones.json");
                cargarCancionesPorTitulo(cancionesObservableList, indicesMap);
              
            });

            Button crearPlaylist = new Button("Playlist");
            crearPlaylist.getStyleClass().add("button");
            crearPlaylist.setPrefHeight(35);

            crearPlaylist.setOnAction(event -> {
                nuevaVentana().show();
            });

            panelSuperior.getChildren().addAll(menuAnadirCancion, crearPlaylist, barraBusqueda);
            HBox.setHgrow(crearPlaylist, Priority.ALWAYS);
            HBox.setHgrow(barraBusqueda, Priority.ALWAYS);

            BorderPane root = new BorderPane();
            root.getStyleClass().add("root");
            root.setTop(panelSuperior);
            root.setLeft(panelIzquierdo);
            root.setCenter(centerPanel);

            onRepeat.setOnAction(event -> {
                reproductor.playOnRepeat();
                albumCover.updateCover(reproductor.getMediaPlayer());
            });
            pausa.setOnAction(event -> {
                reproductor.playPause();
                albumCover.updateCover(reproductor.getMediaPlayer());
            });
            siguiente.setOnAction(event -> {
                reproductor.playNext();
                albumCover.updateCover(reproductor.getMediaPlayer());
            });
            anterior.setOnAction(event -> {
                reproductor.playPrevious();
                albumCover.updateCover(reproductor.getMediaPlayer());
            });
            shuffle.setOnAction(event -> {
                reproductor.playRandom();
                albumCover.updateCover(reproductor.getMediaPlayer());
            });
            verCola.setOnAction(event -> mostrarColaDeReproduccion());

            artista.setOnAction(event -> {
                biblioteca.mostrarPorArtista();
                cargarCancionesPorArtista(cancionesObservableList, indicesMap);
            });

            genero.setOnAction(event -> {
                biblioteca.mostrarPorGenero();
                cargarCancionesPorGenero(cancionesObservableList, indicesMap);
            });

            titulo.setOnAction(event -> {
                biblioteca.mostrarPorTitulo();
                cargarCancionesPorTitulo(cancionesObservableList, indicesMap);
            });

            musicas.setOnAction(event -> {
                biblioteca.mostrarPorTitulo();
                cargarCancionesPorTitulo(cancionesObservableList, indicesMap);
            });

            timelineSlider.setOnMousePressed(e -> {
                reproductor.setUserIsDragging(true);
            });
            timelineSlider.setOnMouseReleased(e -> {
                reproductor.setUserIsDragging(false);
                MediaPlayer mp = reproductor.getMediaPlayer();
                if (mp != null && mp.getTotalDuration() != null) {
                    double percentage = timelineSlider.getValue() / 100.0;
                    Duration total = mp.getTotalDuration();
                    mp.seek(total.multiply(percentage));
                }
            });
            timelineSlider.setOnMouseDragged(e -> {
                reproductor.setUserIsDragging(true);
            });

            listView.setOnMouseClicked(event -> {
                reproductor.setListaActual(reproductor.getListaCanciones());
                int selectedIndex = listView.getSelectionModel().getSelectedIndex();
                if (selectedIndex != -1) {
                    Integer originalIndex = indicesMap.get(selectedIndex);
                    if (originalIndex != null) {
                        MediaPlayer mp = reproductor.getMediaPlayer();
                        if (mp != null) {
                            mp.stop();
                        }
                        reproductor.setIndex(originalIndex);
                        reproductor.playSong(originalIndex);
                        albumCover.updateCover(reproductor.getMediaPlayer());
                    }
                }
            });

            Scene scene = new Scene(root, 990, 620);
            scene.getStylesheets().add(getClass().getResource("/css/estilo.css").toExternalForm());

            primaryStage.setScene(scene);
            primaryStage.show();

            albumCover.updateCover(reproductor.getMediaPlayer());

        } catch (Exception e) {
            e.printStackTrace();
           
        }
    }

    private String formatDuration(Duration duration) {
        int totalSeconds = (int) duration.toSeconds();
        int minutes = totalSeconds / 60;
        int seconds = totalSeconds % 60;
        return String.format("%02d:%02d", minutes, seconds);
    }

    private void mostrarColaDeReproduccion() {
        ListView<String> colaListView = new ListView<>();
        ObservableList<String> colaCanciones = FXCollections.observableArrayList();
        List<File> listaCanciones = reproductor.getListaActual();
        ObservableList<File> observableListaCanciones = FXCollections.observableList(listaCanciones);
        observableListaCanciones.addListener((ListChangeListener<File>) change -> {
            colaCanciones.clear();
            for (File cancion : listaCanciones) {
                colaCanciones.add(cancion.getName());
            }
        });
        for (File cancion : listaCanciones) {
            colaCanciones.add(cancion.getName());
        }
        colaListView.setItems(colaCanciones);

        colaListView.setCellFactory(lv -> new ListCell<>() {
            @Override
            protected void updateItem(String item, boolean empty) {
                super.updateItem(item, empty);
                if (empty || item == null) {
                    setText(null);
                    setGraphic(null);
                } else {
                    HBox cellBox = new HBox(10);
                    cellBox.setAlignment(Pos.CENTER_LEFT);

                    Label songLabel = new Label(item);
                    songLabel.setStyle("-fx-text-fill: white;");

                    Button removeFromQueueButton = new Button();
                    try {
                        ImageView icon = new ImageView(new Image(getClass().getResourceAsStream("/icons/quitarCola.png")));
                        icon.setFitHeight(16);
                        icon.setFitWidth(16);
                        removeFromQueueButton.setGraphic(icon);
                    } catch (Exception e) {
                        Logger.getLogger("MenuPrincipal").log(Level.SEVERE, "Error al cargar el ícono quitarCola.png", e);
                        removeFromQueueButton.setText("-");
                    }
                    removeFromQueueButton.getStyleClass().add("button");

                    removeFromQueueButton.setOnAction(event -> {
                        int selectedIndex = getIndex();
                        if (selectedIndex != -1) {
                            File removedSong = listaCanciones.get(selectedIndex);
                            listaCanciones.remove(selectedIndex);
                            colaCanciones.remove(selectedIndex);
                          
                        }
                    });

                    cellBox.getChildren().setAll(songLabel, removeFromQueueButton);
                    setGraphic(cellBox);
                    setText(null);
                }
            }
        });

        Stage colaStage = new Stage();
        colaStage.setTitle("Cola de Reproducción");

        Button reproducirButton = new Button("Reproducir");
        reproducirButton.getStyleClass().add("button");
        reproducirButton.setOnAction(reproducirEvent -> {
            int selectedIndex = colaListView.getSelectionModel().getSelectedIndex();
            if (selectedIndex != -1) {
                reproductor.stopSong();
                reproductor.setCancionActual(selectedIndex);
                reproductor.playSong(selectedIndex);
                colaStage.close();
            }
        });

        VBox colaLayout = new VBox(10);
        colaLayout.getChildren().addAll(colaListView, reproducirButton);
        colaLayout.setPadding(new Insets(10));

        Scene colaScene = new Scene(colaLayout, 450, 500);
        colaScene.getStylesheets().add(getClass().getResource("/css/estilo.css").toExternalForm());
        colaStage.setScene(colaScene);
        colaStage.show();
    }

    private Stage nuevaVentana() {
        Stage nuevaVentana = new Stage();
        nuevaVentana.setTitle("Gestión de Listas de Reproducción");
        nuevaVentana.setMinWidth(300);
        nuevaVentana.setMinHeight(300);

        double buttonWidth = 200;
        VBox mainLayout = new VBox(15);
        mainLayout.setAlignment(Pos.TOP_CENTER);
        mainLayout.setPadding(new Insets(20));
        mainLayout.setStyle("-fx-background-color: #2c3e50;");

        Label titleLabel = new Label("GESTIÓN DE PLAYLISTS");
        titleLabel.setStyle("-fx-text-fill: white; -fx-font-size: 18px; -fx-font-weight: bold;");

        Button crearListaButton = createStyledButton("Crear Nueva Playlist", buttonWidth);
        Button añadirCancionesButton = createStyledButton("Añadir Canciones", buttonWidth);
        Button eliminarCancionesButton = createStyledButton("Eliminar Canciones", buttonWidth);
        Button mostrarListasButton = createStyledButton("Mostrar Playlists", buttonWidth);
        Button reproducirListaButton = createStyledButton("Reproducir Playlist", buttonWidth);
        Button editarListaButton = createStyledButton("Editar Nombre", buttonWidth);
        Button eliminarListaButton = createStyledButton("Eliminar Playlist", buttonWidth);
        Button volverButton = createStyledButton("Volver", buttonWidth);

        mainLayout.getChildren().addAll(
                titleLabel,
                crearListaButton,
                añadirCancionesButton,
                eliminarCancionesButton,
                mostrarListasButton,
                reproducirListaButton,
                editarListaButton,
                eliminarListaButton,
                volverButton);

        Scene mainScene = new Scene(mainLayout);
        mainScene.getStylesheets().add(getClass().getResource("/css/estilo.css").toExternalForm());
        nuevaVentana.setScene(mainScene);

        volverButton.setOnAction(e -> nuevaVentana.close());

        crearListaButton.setOnAction(e -> {
            VBox createLayout = new VBox(15);
            createLayout.setAlignment(Pos.CENTER);
            createLayout.setPadding(new Insets(20));
            createLayout.setStyle("-fx-background-color: #2c3e50;");

            Label createTitle = new Label("CREAR NUEVA PLAYLIST");
            createTitle.setStyle("-fx-text-fill: white; -fx-font-size: 16px; -fx-font-weight: bold;");

            Label nameLabel = new Label("Nombre de la playlist:");
            nameLabel.setStyle("-fx-text-fill: white;");

            TextField nameInput = new TextField();
            nameInput.setPromptText("Ej: Mis Favoritas");
            nameInput.setMaxWidth(buttonWidth);

            HBox buttonBox = new HBox(10);
            buttonBox.setAlignment(Pos.CENTER);

            Button saveButton = createStyledButton("Guardar", 100);
            Button cancelButton = createStyledButton("Cancelar", 100);

            buttonBox.getChildren().addAll(saveButton, cancelButton);

            saveButton.setOnAction(ev -> {
                String nombre = nameInput.getText().trim();
                if (!nombre.isEmpty()) {
                    biblioteca.crearPlaylist(nombre);
                    nuevaVentana.setScene(mainScene);
                } 
            });

            cancelButton.setOnAction(ev -> nuevaVentana.setScene(mainScene));

            createLayout.getChildren().addAll(createTitle, nameLabel, nameInput, buttonBox);
            Scene createScene = new Scene(createLayout);
            createScene.getStylesheets().add(getClass().getResource("/css/estilo.css").toExternalForm());
            nuevaVentana.setScene(createScene);
        });

        añadirCancionesButton.setOnAction(e -> {
            if (biblioteca.playlist.isEmpty()) {
                return;
            }

            VBox addLayout = new VBox(15);
            addLayout.setAlignment(Pos.CENTER);
            addLayout.setPadding(new Insets(20));
            addLayout.setStyle("-fx-background-color: #2c3e50;");

            Label addTitle = new Label("AÑADIR CANCIONES A PLAYLIST");
            addTitle.setStyle("-fx-text-fill: white; -fx-font-size: 16px; -fx-font-weight: bold;");

            Label selectPlaylistLabel = new Label("Seleccionar playlist:");
            selectPlaylistLabel.setStyle("-fx-text-fill: white;");

            ComboBox<String> playlistCombo = new ComboBox<>();
            playlistCombo.setPromptText("Selecciona una playlist");
            playlistCombo.setMaxWidth(buttonWidth);
            playlistCombo.setItems(FXCollections.observableArrayList(
                    biblioteca.playlist.getPlaylists().stream()
                            .map(lista -> lista.nombre)
                            .toList()));

            Label selectSongsLabel = new Label("Seleccionar canciones:");
            selectSongsLabel.setStyle("-fx-text-fill: white;");

            ListView<String> songsList = new ListView<>();
            songsList.setMaxHeight(200);
            songsList.setMaxWidth(buttonWidth);
            songsList.getSelectionModel().setSelectionMode(SelectionMode.MULTIPLE);
            songsList.setItems(FXCollections.observableArrayList(
                    biblioteca.getListaCanciones().stream()
                            .map(File::getName)
                            .toList()));

            HBox buttonBox = new HBox(10);
            buttonBox.setAlignment(Pos.CENTER);

            Button addButton = createStyledButton("Añadir",100);
            Button backButton = createStyledButton("Volver", 100);

            buttonBox.getChildren().addAll(addButton, backButton);

            addButton.setOnAction(ev -> {
                String selectedPlaylist = playlistCombo.getValue();
                ObservableList<String> selectedSongs = songsList.getSelectionModel().getSelectedItems();

                if (selectedPlaylist == null || selectedSongs.isEmpty()) {
                    return;
                }

                Playlist.Lista lista = biblioteca.playlist.getPlaylists().stream()
                        .filter(p -> p.nombre.equals(selectedPlaylist))
                        .findFirst()
                        .orElse(null);

                if (lista != null) {
                    List<File> cancionesAAgregar = new ArrayList<>();
                    StringBuilder duplicadosMensaje = new StringBuilder();
                    StringBuilder cancionesAñadidas = new StringBuilder();

                    for (String songName : selectedSongs) {
                        biblioteca.getListaCanciones().stream()
                                .filter(file -> file.getName().equals(songName))
                                .findFirst()
                                .ifPresent(file -> {
                                    if (lista.nombreDeCancion.contains(file.getName())) {
                                        duplicadosMensaje.append("La canción ").append(file.getName())
                                                .append(" ya ha sido agregada anteriormente a la lista ")
                                                .append(lista.nombre).append(".\n");
                                    } else {
                                        cancionesAAgregar.add(file);
                                        lista.agregarCancion(file.getName());
                                        cancionesAñadidas.append("Canción ").append(file.getName())
                                                .append(" agregada a la lista ").append(lista.nombre).append(".\n");
                                    }
                                });
                    }

                    biblioteca.playlist.guardarPlaylists();

                    StringBuilder mensajeFinal = new StringBuilder();
                    if (cancionesAAgregar.isEmpty() && duplicadosMensaje.length() > 0) {
                        mensajeFinal.append(duplicadosMensaje).append("Error al añadir.");
                    } else {
                        if (cancionesAñadidas.length() > 0) {
                            mensajeFinal.append(cancionesAñadidas);
                        }
                        if (duplicadosMensaje.length() > 0) {
                            mensajeFinal.append("\n").append(duplicadosMensaje).append("Error al añadir.");
                        }
                    }

                    if (mensajeFinal.length() > 0) {
                    }
                }
            });

            backButton.setOnAction(ev -> nuevaVentana.setScene(mainScene));

            addLayout.getChildren().addAll(
                    addTitle, selectPlaylistLabel, playlistCombo,
                    selectSongsLabel, songsList, buttonBox);

            Scene addScene = new Scene(addLayout);
            addScene.getStylesheets().add(getClass().getResource("/css/estilo.css").toExternalForm());
            nuevaVentana.setScene(addScene);
        });

        eliminarCancionesButton.setOnAction(e -> {
            if (biblioteca.playlist.isEmpty()) {
             
                return;
            }

            VBox removeLayout = new VBox(15);
            removeLayout.setAlignment(Pos.CENTER);
            removeLayout.setPadding(new Insets(20));
            removeLayout.setStyle("-fx-background-color: #2c3e50;");

            Label removeTitle = new Label("ELIMINAR CANCIONES DE PLAYLIST");
            removeTitle.setStyle("-fx-text-fill: white; -fx-font-size: 16px; -fx-font-weight: bold;");

            Label selectPlaylistLabel = new Label("Seleccionar playlist:");
            selectPlaylistLabel.setStyle("-fx-text-fill: white;");

            ComboBox<String> playlistCombo = new ComboBox<>();
            playlistCombo.setPromptText("Selecciona una playlist");
            playlistCombo.setMaxWidth(buttonWidth);
            playlistCombo.setItems(FXCollections.observableArrayList(
                    biblioteca.playlist.getPlaylists().stream()
                            .map(lista -> lista.nombre)
                            .toList()));

            Label songsLabel = new Label("Canciones en la playlist:");
            songsLabel.setStyle("-fx-text-fill: white;");

            ListView<String> songsList = new ListView<>();
            songsList.setMaxHeight(200);
            songsList.setMaxWidth(buttonWidth);
            songsList.getSelectionModel().setSelectionMode(SelectionMode.SINGLE);

            playlistCombo.setOnAction(ev -> {
                String selected = playlistCombo.getValue();
                if (selected != null) {
                    Playlist.Lista lista = biblioteca.playlist.getPlaylists().stream()
                            .filter(p -> p.nombre.equals(selected))
                            .findFirst()
                            .orElse(null);

                    if (lista != null) {
                        songsList.setItems(FXCollections.observableArrayList(lista.nombreDeCancion));
                    }
                }
            });

            HBox buttonBox = new HBox(10);
            buttonBox.setAlignment(Pos.CENTER);

            Button removeButton = createStyledButton("Eliminar", 100);
            Button backButton = createStyledButton("Volver", 100);

            buttonBox.getChildren().addAll(removeButton, backButton);

            removeButton.setOnAction(ev -> {
                String selectedPlaylist = playlistCombo.getValue();
                String selectedSong = songsList.getSelectionModel().getSelectedItem();

                if (selectedPlaylist == null || selectedSong == null) {
                    return;
                }

                Alert confirmation = new Alert(Alert.AlertType.CONFIRMATION);
                confirmation.setTitle("Confirmar eliminación");
                confirmation.setHeaderText("Eliminar canción");
                confirmation.setContentText("¿Estás seguro de que quieres eliminar '" +
                        selectedSong + "' de '" + selectedPlaylist + "'?");

                Optional<ButtonType> result = confirmation.showAndWait();
                if (result.isPresent() && result.get() == ButtonType.OK) {
                    Playlist.Lista lista = biblioteca.playlist.getPlaylists().stream()
                            .filter(p -> p.nombre.equals(selectedPlaylist))
                            .findFirst()
                            .orElse(null);

                    if (lista != null && lista.nombreDeCancion.remove(selectedSong)) {
                        biblioteca.playlist.guardarPlaylists();
                        songsList.setItems(FXCollections.observableArrayList(lista.nombreDeCancion));
                    }
                }
            });

            backButton.setOnAction(ev -> nuevaVentana.setScene(mainScene));

            removeLayout.getChildren().addAll(
                    removeTitle, selectPlaylistLabel, playlistCombo,
                    songsLabel, songsList, buttonBox);

            Scene removeScene = new Scene(removeLayout);
            removeScene.getStylesheets().add(getClass().getResource("/css/estilo.css").toExternalForm());
            nuevaVentana.setScene(removeScene);
        });

        mostrarListasButton.setOnAction(e -> {
            VBox showLayout = new VBox(15);
            showLayout.setAlignment(Pos.CENTER);
            showLayout.setPadding(new Insets(20));
            showLayout.setStyle("-fx-background-color: #2c3e50;");

            Label showTitle = new Label("TUS PLAYLISTS");
            showTitle.setStyle("-fx-text-fill: white; -fx-font-size: 16px; -fx-font-weight: bold;");

            TextArea playlistsText = new TextArea();
            playlistsText.setEditable(false);
            playlistsText.setWrapText(true);
            playlistsText.setMaxWidth(350);
            playlistsText.setMaxHeight(300);
            playlistsText.setStyle("-fx-font-family: 'Consolas'; -fx-font-size: 12px;");

            StringBuilder sb = new StringBuilder();
            if (biblioteca.playlist.isEmpty()) {
                sb.append("No tienes playlists creadas.\n");
                sb.append("¡Usa el botón 'Crear Nueva Playlist' para empezar!");
            } else {
                sb.append("Total playlists: ").append(biblioteca.playlist.size()).append("\n\n");

                for (int i = 0; i < biblioteca.playlist.size(); i++) {
                    Playlist.Lista lista = biblioteca.playlist.get(i);
                    sb.append("══════════════════════════════════════\n");
                    sb.append("Playlist #").append(i + 1).append(": ").append(lista.nombre).append("\n");
                    sb.append("Canciones: ").append(lista.nombreDeCancion.size()).append("\n");

                    if (lista.nombreDeCancion.isEmpty()) {
                        sb.append("  (Esta playlist está vacía)\n");
                    } else {
                        for (int j = 0; j < lista.nombreDeCancion.size(); j++) {
                            sb.append(String.format("  %2d. %s%n", j + 1, lista.nombreDeCancion.get(j)));
                        }
                    }
                    sb.append("══════════════════════════════════════\n\n");
                }
            }
            playlistsText.setText(sb.toString());

            Button backButton = createStyledButton("Volver", buttonWidth);

            backButton.setOnAction(ev -> nuevaVentana.setScene(mainScene));

            showLayout.getChildren().addAll(showTitle, playlistsText, backButton);
            Scene showScene = new Scene(showLayout);
            showScene.getStylesheets().add(getClass().getResource("/css/estilo.css").toExternalForm());
            nuevaVentana.setScene(showScene);
        });

        reproducirListaButton.setOnAction(e -> {
            if (biblioteca.playlist.isEmpty()) {
                return;
            }

            VBox playLayout = new VBox(15);
            playLayout.setAlignment(Pos.CENTER);
            playLayout.setPadding(new Insets(20));
            playLayout.setStyle("-fx-background-color: #2c3e50;");

            Label playTitle = new Label("REPRODUCIR PLAYLIST");
            playTitle.setStyle("-fx-text-fill: white; -fx-font-size: 16px; -fx-font-weight: bold;");

            Label selectPlaylistLabel = new Label("Seleccionar playlist:");
            selectPlaylistLabel.setStyle("-fx-text-fill: white;");

            ComboBox<String> playlistCombo = new ComboBox<>();
            playlistCombo.setPromptText("Selecciona una playlist");
            playlistCombo.setMaxWidth(buttonWidth);
            playlistCombo.setItems(FXCollections.observableArrayList(
                    biblioteca.playlist.getPlaylists().stream()
                            .map(lista -> lista.nombre)
                            .toList()));

            Label songsLabel = new Label("Canciones en la playlist:");
            songsLabel.setStyle("-fx-text-fill: white;");

            ListView<String> songsList = new ListView<>();
            songsList.setMaxHeight(200);
            songsList.setMaxWidth(buttonWidth);

            playlistCombo.setOnAction(ev -> {
                String selected = playlistCombo.getValue();
                if (selected != null) {
                    Playlist.Lista lista = biblioteca.playlist.getPlaylists().stream()
                            .filter(p -> p.nombre.equals(selected))
                            .findFirst()
                            .orElse(null);

                    if (lista != null) {
                        songsList.setItems(FXCollections.observableArrayList(lista.nombreDeCancion));
                    }
                }
            });

            HBox buttonBox = new HBox(10);
            buttonBox.setAlignment(Pos.CENTER);

            Button playButton = createStyledButton("Reproducir", 100);
            Button backButton = createStyledButton("Volver", 100);

            buttonBox.getChildren().addAll(playButton, backButton);

            playButton.setOnAction(ev -> {
                String selectedPlaylistName = playlistCombo.getValue();
                String selectedSongName = songsList.getSelectionModel().getSelectedItem();

                if (selectedPlaylistName == null) {
                    return;
                }

                Playlist.Lista selectedPlaylist = biblioteca.playlist.getPlaylists().stream()
                        .filter(p -> p.nombre.equals(selectedPlaylistName))
                        .findFirst()
                        .orElse(null);

                if (selectedPlaylist != null) {
                    if (selectedPlaylist.nombreDeCancion.isEmpty()) {
                        return;
                    }

                    List<File> cancionesPlaylist = new ArrayList<>();
                    int startIndex = 0;

                    for (int i = 0; i < selectedPlaylist.nombreDeCancion.size(); i++) {
                        String nombreCancion = selectedPlaylist.nombreDeCancion.get(i);
                        File archivoCancion = biblioteca.getListaCanciones().stream()
                                .filter(file -> file.getName().equals(nombreCancion))
                                .findFirst()
                                .orElse(null);

                        if (archivoCancion != null) {
                            cancionesPlaylist.add(archivoCancion);
                            if (nombreCancion.equals(selectedSongName)) {
                                startIndex = i;
                            }
                        }
                    }

                    if (!cancionesPlaylist.isEmpty()) {
                        reproductor.setListaActual(cancionesPlaylist);
                        reproductor.setCancionActual(startIndex);
                        nuevaVentana.close();
                     
                    } 
                    
                }
            });

            backButton.setOnAction(ev -> nuevaVentana.setScene(mainScene));

            playLayout.getChildren().addAll(
                    playTitle, selectPlaylistLabel, playlistCombo,
                    songsLabel, songsList, buttonBox);

            Scene playScene = new Scene(playLayout);
            playScene.getStylesheets().add(getClass().getResource("/css/estilo.css").toExternalForm());
            nuevaVentana.setScene(playScene);
        });

        editarListaButton.setOnAction(e -> {
            if (biblioteca.playlist.isEmpty()) {
                return;
            }

            VBox editLayout = new VBox(15);
            editLayout.setAlignment(Pos.CENTER);
            editLayout.setPadding(new Insets(20));
            editLayout.setStyle("-fx-background-color: #2c3e50;");

            Label editTitle = new Label("EDITAR NOMBRE DE PLAYLIST");
            editTitle.setStyle("-fx-text-fill: white; -fx-font-size: 16px; -fx-font-weight: bold;");

            Label selectPlaylistLabel = new Label("Seleccionar playlist:");
            selectPlaylistLabel.setStyle("-fx-text-fill: white;");

            ComboBox<String> playlistCombo = new ComboBox<>();
            playlistCombo.setPromptText("Selecciona una playlist");
            playlistCombo.setMaxWidth(buttonWidth);
            playlistCombo.setItems(FXCollections.observableArrayList(
                    biblioteca.playlist.getPlaylists().stream()
                            .map(lista -> lista.nombre)
                            .toList()));

            Label newNameLabel = new Label("Nuevo nombre:");
            newNameLabel.setStyle("-fx-text-fill: white;");

            TextField newNameField = new TextField();
            newNameField.setPromptText("Nuevo nombre para la playlist");
            newNameField.setMaxWidth(buttonWidth);

            HBox buttonBox = new HBox(10);
            buttonBox.setAlignment(Pos.CENTER);

            Button saveButton = createStyledButton("Guardar", 100);
            Button backButton = createStyledButton("Volver", 100);

            buttonBox.getChildren().addAll(saveButton, backButton);

            saveButton.setOnAction(ev -> {
                String selectedPlaylist = playlistCombo.getValue();
                String newName = newNameField.getText().trim();

                if (selectedPlaylist == null || newName.isEmpty()) {

                    return;
                }

                if (newName.equals(selectedPlaylist)) {
                    return;
                }

                boolean nameExists = biblioteca.playlist.getPlaylists().stream()
                        .anyMatch(p -> p.nombre.equalsIgnoreCase(newName));

                if (nameExists) {                
                    return;
                }

                Playlist.Lista lista = biblioteca.playlist.getPlaylists().stream()
                        .filter(p -> p.nombre.equals(selectedPlaylist))
                        .findFirst()
                        .orElse(null);

                if (lista != null) {
                    String oldName = lista.nombre;
                    lista.nombre = newName;
                    biblioteca.playlist.guardarPlaylists();

                    playlistCombo.setItems(FXCollections.observableArrayList(
                            biblioteca.playlist.getPlaylists().stream()
                                    .map(p -> p.nombre)
                                    .toList()));
                    playlistCombo.setValue(newName);

                
                    newNameField.clear();
                }
            });

            backButton.setOnAction(ev -> nuevaVentana.setScene(mainScene));

            editLayout.getChildren().addAll(
                    editTitle, selectPlaylistLabel, playlistCombo,
                    newNameLabel, newNameField, buttonBox);

            Scene editScene = new Scene(editLayout);
            editScene.getStylesheets().add(getClass().getResource("/css/estilo.css").toExternalForm());
            nuevaVentana.setScene(editScene);
        });

        eliminarListaButton.setOnAction(e -> {
            if (biblioteca.playlist.isEmpty()) {
                
                return;
            }

            VBox deleteLayout = new VBox(15);
            deleteLayout.setAlignment(Pos.CENTER);
            deleteLayout.setPadding(new Insets(20));
            deleteLayout.setStyle("-fx-background-color: #2c3e50;");

            Label deleteTitle = new Label("ELIMINAR PLAYLIST");
            deleteTitle.setStyle("-fx-text-fill: white; -fx-font-size: 16px; -fx-font-weight: bold;");

            Label selectPlaylistLabel = new Label("Seleccionar playlist:");
            selectPlaylistLabel.setStyle("-fx-text-fill: white;");

            ComboBox<String> playlistCombo = new ComboBox<>();
            playlistCombo.setPromptText("Selecciona una playlist");
            playlistCombo.setMaxWidth(buttonWidth);
            playlistCombo.setItems(FXCollections.observableArrayList(
                    biblioteca.playlist.getPlaylists().stream()
                            .map(lista -> lista.nombre)
                            .toList()));

            HBox buttonBox = new HBox(10);
            buttonBox.setAlignment(Pos.CENTER);

            Button deleteButton = createStyledButton("Eliminar", 100);
            Button backButton = createStyledButton("Volver", 100);

            buttonBox.getChildren().addAll(deleteButton, backButton);

            deleteButton.setOnAction(ev -> {
                String selectedPlaylist = playlistCombo.getValue();

                if (selectedPlaylist == null) {
                    return;
                }

                Alert confirmation = new Alert(Alert.AlertType.CONFIRMATION);
                confirmation.setTitle("Confirmar eliminación");
                confirmation.setHeaderText("Eliminar playlist");
                confirmation.setContentText("¿Estás seguro de que quieres eliminar la playlist '" +
                        selectedPlaylist + "'? Esta acción no se puede deshacer.");

                Optional<ButtonType> result = confirmation.showAndWait();
                if (result.isPresent() && result.get() == ButtonType.OK) {
                    boolean removed = biblioteca.playlist.getPlaylists().removeIf(
                            p -> p.nombre.equals(selectedPlaylist));

                    if (removed) {
                        biblioteca.playlist.guardarPlaylists();

                        playlistCombo.setItems(FXCollections.observableArrayList(
                                biblioteca.playlist.getPlaylists().stream()
                                        .map(p -> p.nombre)
                                        .toList()));
                        playlistCombo.setValue(null);

                    }
                }
            });

            backButton.setOnAction(ev -> nuevaVentana.setScene(mainScene));

            deleteLayout.getChildren().addAll(
                    deleteTitle, selectPlaylistLabel, playlistCombo, buttonBox);

            Scene deleteScene = new Scene(deleteLayout);
            deleteScene.getStylesheets().add(getClass().getResource("/css/estilo.css").toExternalForm());
            nuevaVentana.setScene(deleteScene);
        });

        return nuevaVentana;
    }

    private Button createStyledButton(String text, double width) {
        Button button = new Button(text);
        button.setPrefWidth(width);
        button.getStyleClass().add("button");
        return button;
    }

    private void actualizarHistorial(ComboBox<String> historialBusquedas) {
        ObservableList<String> items = FXCollections.observableArrayList(buscador.getHistorialBusquedas());
        historialBusquedas.setItems(items);
    }

    private void mostrarResultadosBusqueda(List<File> resultados, ObservableList<String> cancionesObservableList,
                                           Map<Integer, Integer> indicesMap) {
        cancionesObservableList.clear();
        indicesMap.clear();
        int index = 0;
        for (File cancion : resultados) {
            cancionesObservableList.add(cancion.getName());
            indicesMap.put(index++, biblioteca.getListaCanciones().indexOf(cancion));
        }
    }

    private void cargarCancionesPorArtista(ObservableList<String> cancionesObservableList,
                                           Map<Integer, Integer> indicesMap) {
        cancionesObservableList.clear();
        indicesMap.clear();
        int index = 0;
        cancionesObservableList.add("ARTISTAS");
        indicesMap.put(index++, -1);

        for (Map.Entry<String, List<File>> entry : biblioteca.getCancionesPorArtista().entrySet()) {
            String artista = entry.getKey();
            cancionesObservableList.add("  " + artista);
            indicesMap.put(index++, -1);

            for (File cancion : entry.getValue()) {
                String nombre = "    " + cancion.getName();
                if (!cancionesObservableList.contains(nombre)) {
                    cancionesObservableList.add(nombre);
                    indicesMap.put(index++, biblioteca.getListaCanciones().indexOf(cancion));
                }
            }
        }
    }

    private void cargarCancionesPorGenero(ObservableList<String> cancionesObservableList,
                                          Map<Integer, Integer> indicesMap) {
        cancionesObservableList.clear();
        indicesMap.clear();
        int index = 0;
        cancionesObservableList.add("GÉNEROS");
        indicesMap.put(index++, -1);
        for (Map.Entry<String, List<File>> entry : biblioteca.getCancionesPorGenero().entrySet()) {
            String genero = entry.getKey();
            cancionesObservableList.add("  " + genero);
            indicesMap.put(index++, -1);

            for (File cancion : entry.getValue()) {
                String nombre = "    " + cancion.getName();
                if (!cancionesObservableList.contains(nombre)) {
                    cancionesObservableList.add(nombre);
                    indicesMap.put(index++, biblioteca.getListaCanciones().indexOf(cancion));
                }
            }
        }
    }

    private void cargarCancionesPorTitulo(ObservableList<String> cancionesObservableList,
                                          Map<Integer, Integer> indicesMap) {
        cancionesObservableList.clear();
        indicesMap.clear();
        int index = 0;
        for (File cancion : biblioteca.getListaCanciones()) {
            if (!cancionesObservableList.contains(cancion.getName())) {
                cancionesObservableList.add(cancion.getName());
                indicesMap.put(index++, biblioteca.getListaCanciones().indexOf(cancion));
            }
        }
    }

}
