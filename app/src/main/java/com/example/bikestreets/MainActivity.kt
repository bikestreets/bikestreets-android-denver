package com.example.bikestreets

// used to handle geojson loading
import java.io.InputStream
import android.content.res.AssetManager

// used to handle geojson map layer drawing
import com.mapbox.geojson.FeatureCollection
import com.mapbox.mapboxsdk.style.layers.LineLayer
import com.mapbox.mapboxsdk.style.layers.Property
import com.mapbox.mapboxsdk.style.layers.PropertyFactory
import android.graphics.Color
import com.mapbox.mapboxsdk.style.sources.GeoJsonSource

import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import com.mapbox.android.core.permissions.PermissionsListener
import com.mapbox.android.core.permissions.PermissionsManager
import com.mapbox.mapboxsdk.Mapbox
import com.mapbox.mapboxsdk.location.LocationComponentActivationOptions
import com.mapbox.mapboxsdk.location.LocationComponentOptions
import com.mapbox.mapboxsdk.location.modes.CameraMode
import com.mapbox.mapboxsdk.location.modes.RenderMode
import com.mapbox.mapboxsdk.maps.MapView
import com.mapbox.mapboxsdk.maps.MapboxMap
import com.mapbox.mapboxsdk.maps.Style
import java.util.*


class MainActivity : AppCompatActivity() {
    private var mapView: MapView? = null
    private var permissionsManager: PermissionsManager ?= null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        Mapbox.getInstance(this, "pk.eyJ1Ijoianpvcm5vdyIsImEiOiJjazVsOWhkc2YwbWgwM2xuNXJvdnlhN2o3In0.tW5TbWlDY-ciFrYSv6qTOA")

        setContentView(R.layout.activity_main)

        mapView = findViewById(R.id.mapView)
        mapView?.onCreate(savedInstanceState)
        mapView?.getMapAsync { mapboxMap ->
            mapboxMap.setStyle(Style.MAPBOX_STREETS) {
                // Map is set up and the style has loaded. Now you can add data or make other map adjustments
                showDeviceLocation(mapboxMap, it)

                // add geojson layers
                showMapLayers(this, it)
            }
        }

        if (PermissionsManager.areLocationPermissionsGranted(this)){
            // Permission sensitive logic called here, such as activating the Maps SDK's LocationComponent to show the device's location
        } else {
            var permissionsListener: PermissionsListener = object : PermissionsListener {
                override fun onExplanationNeeded(permissionsToExplain: List<String>) {
                    // provides explanation of why permission is required
                }

                override fun onPermissionResult(granted: Boolean) {
                    if (granted) {
                        // Permission sensitive logic called here, such as activating the Maps SDK's LocationComponent to show the device's location


                    } else {
                        // User denied the permission
                    }
                }
            }

            permissionsManager = PermissionsManager(permissionsListener)
            permissionsManager?.requestLocationPermissions(this)
        }
    }

    fun showMapLayers(activity: MainActivity, mapStyle: Style) {
        var mAssetManager: AssetManager = activity.getAssets()
        val root: String = "geojson"

        mAssetManager.list("$root/").forEach { fileName ->
            var featureCollection = featureCollectionFromStream(
                mAssetManager.open("$root/$fileName")
            )

            renderFeatureCollection(fileName, featureCollection, mapStyle)
        }

    }

    fun featureCollectionFromStream(fileStream: InputStream): FeatureCollection {
        var geoJsonString = convertStreamToString(fileStream)

        return FeatureCollection.fromJson(geoJsonString)
    }

    fun colorForLayer(layerName: String): Int {
        // This is lazy coupling and will break, but I want to see it work as a proof-of-concept.
        // A more flexible refactor involves inspecting the GeoJson file itself to get the layer
        // name, then matching the color based on that (or we can save the layer color as metadata.)
        val hexColor = when(layerName) {
            "1-bikestreets-master-v0.3.geojson" -> "#061f78"
            "3-bikelanes-master-v0.3.geojson" -> "#b00d0d"
            "5-walk-master-v0.3.geojson" -> "#c9c219"
            "2-trails-master-v0.3.geojson" -> "#eea800"
            "4-bikesidewalks-master-v0.3.geojson" -> "#1500f2"
            else -> "#000000"
        }

        return Color.parseColor(hexColor)
    }

    fun createLineLayer(layerName: String): LineLayer {
        val lineColor = colorForLayer(layerName)

        return LineLayer("$layerName-id", layerName)
            .withProperties(
                PropertyFactory.lineCap(Property.LINE_CAP_SQUARE),
                PropertyFactory.lineJoin(Property.LINE_JOIN_MITER),
                PropertyFactory.lineOpacity(.7f),
                PropertyFactory.lineWidth(7f),
                PropertyFactory.lineColor(lineColor))
    }

    fun renderFeatureCollection(layerName: String, featureCollection: FeatureCollection, mapStyle: Style) {
        if(featureCollection.features() != null) {
            // add the data itself to mapStyle
            mapStyle.addSource(GeoJsonSource(layerName, featureCollection))

            // create a line layer that reads the GeoJSON data that we just added
            mapStyle.addLayer(createLineLayer(layerName))
        }
    }

    fun convertStreamToString(input: InputStream): String {
        val scanner = Scanner(input).useDelimiter("\\A")
        return if (scanner.hasNext()) scanner.next() else ""
    }

    fun showDeviceLocation(mapboxMap: MapboxMap, style: Style) {

        val locationComponentOptions = LocationComponentOptions
            .builder(this)
            .build()

        val locationComponentActivationOptions = LocationComponentActivationOptions
            .builder(this, style)
            .locationComponentOptions(locationComponentOptions)
            .build()

        var locationComponent = mapboxMap.locationComponent

        // Activate with options
        locationComponent.activateLocationComponent(locationComponentActivationOptions)

        // Enable to make component visible
        locationComponent.setLocationComponentEnabled(true)

        // Set the component's camera mode
        locationComponent.setCameraMode(CameraMode.TRACKING, 10, 15.0, null, null, null)

        // Set the component's render mode
        locationComponent.setRenderMode(RenderMode.COMPASS)
    }

    override fun onStart() {
        super.onStart()
        mapView?.onStart()
    }

    override fun onResume() {
        super.onResume()
        mapView?.onResume()
    }

    override fun onPause() {
        super.onPause()
        mapView?.onPause()
    }

    override fun onStop() {
        super.onStop()
        mapView?.onStop()
    }

    override fun onLowMemory() {
        super.onLowMemory()
        mapView?.onLowMemory()
    }

    override fun onDestroy() {
        super.onDestroy()
        mapView?.onDestroy()
    }

    override fun onSaveInstanceState(outState: Bundle) {
        super.onSaveInstanceState(outState)
        mapView?.onSaveInstanceState(outState)
    }
}

