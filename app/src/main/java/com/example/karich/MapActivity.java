package com.example.karich;

import android.os.Bundle;
import android.view.View;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.ListView;

import androidx.annotation.NonNull;
import androidx.fragment.app.FragmentActivity;

import com.yandex.mapkit.MapKitFactory;
import com.yandex.mapkit.geometry.Point;
import com.yandex.mapkit.map.CameraPosition;
import com.yandex.mapkit.mapview.MapView;
import com.yandex.mapkit.user_location.UserLocationLayer;
import com.yandex.mapkit.user_location.UserLocationObjectListener;
import com.yandex.mapkit.geometry.Point;
import com.yandex.mapkit.map.CameraPosition;
import com.yandex.mapkit.mapview.MapView;
import com.yandex.mapkit.user_location.UserLocationLayer;
import com.yandex.mapkit.user_location.UserLocationObjectListener;
import com.yandex.mapkit.user_location.UserLocationView;


import java.util.ArrayList;
import java.util.List;

public class MapActivity extends FragmentActivity {

    private MapView mapView;
    private UserLocationLayer userLocationLayer;
    private ListView listNearbyStations;
    private Button btnShowStations;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        // Инициализация Yandex Maps SDK
        MapKitFactory.setApiKey("ae031c72-47c5-46c9-98bf-8a590bd2b0ee");
        MapKitFactory.initialize(this);
        setContentView(R.layout.activity_map);

        mapView = findViewById(R.id.map_view);
        listNearbyStations = findViewById(R.id.list_nearby_stations);
        btnShowStations = findViewById(R.id.btn_show_stations);

        // Инициализация слоя с местоположением пользователя
        userLocationLayer = MapKitFactory.getInstance().createUserLocationLayer(mapView.getMapWindow());
        userLocationLayer.setVisible(true);
        userLocationLayer.setHeadingEnabled(true);

        // Центрирование карты на текущем местоположении пользователя



        btnShowStations.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                listNearbyStations.setVisibility(View.VISIBLE);
                // Загрузка ближайших заправок
                loadNearbyStations();
            }
        });

        // Добавление маркеров заправок
        addStationsMarkers();
    }

    private void addStationsMarkers() {
        List<Point> stations = getNearbyStations();
        for (Point station : stations) {
            mapView.getMap().getMapObjects().addPlacemark(station);
        }
    }

    private List<Point> getNearbyStations() {
        // Пример получения списка ближайших заправок
        List<Point> stations = new ArrayList<>();
        stations.add(new Point(55.751244, 37.618423)); // Пример координат
        stations.add(new Point(55.7558, 37.6176));
        // Добавьте больше заправок по мере необходимости
        return stations;
    }

    private void loadNearbyStations() {
        // Пример загрузки списка ближайших заправок
        List<String> stations = new ArrayList<>();
        stations.add("Заправка 1");
        stations.add("Заправка 2");
        stations.add("Заправка 3");
        stations.add("Заправка 4");
        stations.add("Заправка 5");

        ArrayAdapter<String> adapter = new ArrayAdapter<>(this, android.R.layout.simple_list_item_1, stations);
        listNearbyStations.setAdapter(adapter);
    }

    @Override
    protected void onStop() {
        mapView.onStop();
        MapKitFactory.getInstance().onStop();
        super.onStop();
    }

    @Override
    protected void onStart() {
        super.onStart();
        MapKitFactory.getInstance().onStart();
        mapView.onStart();
    }
}
