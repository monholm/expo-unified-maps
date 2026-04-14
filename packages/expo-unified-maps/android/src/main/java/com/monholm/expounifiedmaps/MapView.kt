package com.monholm.expounifiedmaps

import android.content.Context
import android.graphics.drawable.BitmapDrawable
import android.view.View.MeasureSpec
import expo.modules.kotlin.AppContext
import expo.modules.kotlin.Promise
import expo.modules.kotlin.views.ExpoView
import com.google.android.gms.maps.CameraUpdateFactory
import com.google.android.gms.maps.GoogleMap
import com.google.android.gms.maps.model.BitmapDescriptorFactory
import com.google.android.gms.maps.model.MarkerOptions
import com.google.maps.android.collections.MarkerManager
import expo.modules.kotlin.viewevent.EventDispatcher

class MapView(context: Context, appContext: AppContext) : ExpoView(context, appContext) {
  val onMapClick by EventDispatcher()
  val onMarkerClick by EventDispatcher()
  internal val mapView = com.google.android.gms.maps.MapView(context)
  private var googleMap: GoogleMap? = null
  private var markerManager: MarkerManager? = null
  private var defaultMarkerCollection: MarkerManager.Collection? = null

  private var initialRegionApplied = false
  private val currentMarkers = mutableMapOf<String, com.google.android.gms.maps.model.Marker>()

  // Operations that only need the GoogleMap instance are queued until getMapAsync completes.
  // Camera operations additionally require a completed layout (size > 0).
  private val pendingMapActions = mutableListOf<(GoogleMap) -> Unit>()
  private val pendingCameraActions = mutableListOf<(GoogleMap) -> Unit>()

  private fun runWhenMapReady(action: (GoogleMap) -> Unit) {
    val map = googleMap
    if (map != null) action(map)
    else pendingMapActions.add(action)
  }

  private fun runWhenCameraReady(action: (GoogleMap) -> Unit) {
    val map = googleMap
    if (map != null && mapView.width > 0 && mapView.height > 0) action(map)
    else pendingCameraActions.add(action)
  }

  private fun drainMapQueue() {
    val map = googleMap ?: return
    val snapshot = pendingMapActions.toList()
    pendingMapActions.clear()
    for (action in snapshot) action(map)
  }

  private fun drainCameraQueue() {
    val map = googleMap ?: return
    if (mapView.width <= 0 || mapView.height <= 0) return
    val snapshot = pendingCameraActions.toList()
    pendingCameraActions.clear()
    for (action in snapshot) action(map)
  }

  init {
    addView(mapView)
    mapView.onCreate(null)
    mapView.onResume()
    mapView.addOnLayoutChangeListener { _, _, _, _, _, _, _, _, _ ->
      drainCameraQueue()
    }
    mapView.getMapAsync {
      this.googleMap = it
      val markerManager = MarkerManager(it)
      this.markerManager = markerManager
      val defaultMarkerCollection = markerManager.newCollection()
      this.defaultMarkerCollection = defaultMarkerCollection

      // Disable toolbar to align with iOS behavior
      it.uiSettings.isMapToolbarEnabled = false

      // Set up event listeners
      it.setOnMapClickListener { latLng ->
        val point = it.projection.toScreenLocation(latLng)
        val density = context.resources.displayMetrics.density
        onMapClick(mapOf(
          "coordinate" to mapOf("latitude" to latLng.latitude, "longitude" to latLng.longitude),
          "point" to mapOf("x" to (point.x / density).toDouble(), "y" to (point.y / density).toDouble())
        ))
      }

      defaultMarkerCollection.setOnMarkerClickListener { marker ->
        val id = currentMarkers.entries.firstOrNull { it.value == marker }?.key
        if (id != null) {
          onMarkerClick(mapOf(
            "id" to id,
            "coordinate" to mapOf(
              "latitude" to marker.position.latitude,
              "longitude" to marker.position.longitude
            )
          ))
        }
        // Return true to consume the event and thus prevent the default behavior (showing the info window and centering the map on the marker)
        // This is done to align with the default behaviour on iOS
        true
      }

      drainMapQueue()
      drainCameraQueue()
    }
  }

  fun setInitialRegion(region: Region?) {
    if (initialRegionApplied || region == null) return
    initialRegionApplied = true
    runWhenCameraReady { map ->
      val bounds = region.toLatLngBounds()
      map.moveCamera(CameraUpdateFactory.newLatLngBounds(bounds, 0))
    }
  }

  fun setShowCompass(enabled: Boolean?) {
    enabled?.let { isEnabled ->
      runWhenMapReady { map ->
        map.uiSettings.isCompassEnabled = isEnabled
      }
    }
  }

  fun setZoomEnabled(enabled: Boolean?) {
    enabled?.let { isEnabled ->
      runWhenMapReady { map ->
        map.uiSettings.isZoomGesturesEnabled = isEnabled
      }
    }
  }

  fun setScrollEnabled(enabled: Boolean?) {
    enabled?.let { isEnabled ->
      runWhenMapReady { map ->
        map.uiSettings.isScrollGesturesEnabled = isEnabled
      }
    }
  }

  fun setRotateEnabled(enabled: Boolean?) {
    enabled?.let { isEnabled ->
      runWhenMapReady { map ->
        map.uiSettings.isRotateGesturesEnabled = isEnabled
      }
    }
  }

  fun setPitchEnabled(enabled: Boolean?) {
    enabled?.let { isEnabled ->
      runWhenMapReady { map ->
        map.uiSettings.isTiltGesturesEnabled = isEnabled
      }
    }
  }

  fun setRegion(options: SetRegionOptions, promise: Promise) {
    runWhenCameraReady { map ->
      applyRegion(map, options) {
        promise.resolve(null)
      }
    }
  }

  fun setMapPadding(padding: Padding?) {
    padding?.let { p ->
      runWhenMapReady { map ->
        applyMapPadding(map, p)
      }
    }
  }

  fun setBoundary(region: Region?) {
    runWhenCameraReady { map ->
      applyBoundary(map, region)
    }
  }

  fun setMarkers(markers: Array<Marker>?) {
    runWhenMapReady {
      applyMarkers(markers ?: emptyArray())
    }
  }

  private fun applyMarkers(incoming: Array<Marker>) {
    val collection = defaultMarkerCollection ?: return
    val incomingIds = incoming.map { it.id }.toSet()

    // Remove stale markers
    val staleIds = currentMarkers.keys - incomingIds
    for (id in staleIds) {
      currentMarkers.remove(id)?.let { collection.remove(it) }
    }

    // Add new and update existing
    for (marker in incoming) {
      (marker.icon?.ref as? BitmapDrawable)?.bitmap?.let { bitmap ->
        val icon = BitmapDescriptorFactory.fromBitmap(bitmap)
        val existing = currentMarkers[marker.id]
        if (existing != null) {
          existing.position = marker.coordinate.toGoogleLatLng()
          existing.setIcon(icon)
        } else {
          val options = MarkerOptions()
            .position(marker.coordinate.toGoogleLatLng())
            .icon(icon)
          currentMarkers[marker.id] = collection.addMarker(options)
        }
      }
    }
  }

  private fun applyMapPadding(map: GoogleMap, padding: Padding) {
    val density = context.resources.displayMetrics.density
    map.setPadding(
      (padding.left * density).toInt(),
      (padding.top * density).toInt(),
      (padding.right * density).toInt(),
      (padding.bottom * density).toInt()
    )
    // Force the MapView to reposition its built-in UI controls (compass, Google logo, etc.)
    // as setPadding alone doesn't move them when called after the initial layout,
    // they are simply clipped by the new padding.
    // React Native's Yoga layout overrides requestLayout() as a no-op, so we directly
    // trigger a measure+layout pass — both are required for the Maps SDK to respond.
    mapView.measure(
      MeasureSpec.makeMeasureSpec(mapView.width, MeasureSpec.EXACTLY),
      MeasureSpec.makeMeasureSpec(mapView.height, MeasureSpec.EXACTLY)
    )
    mapView.layout(mapView.left, mapView.top, mapView.right, mapView.bottom)
  }

  private fun applyBoundary(map: GoogleMap, region: Region?) {
    region?.let { boundaryRegion ->
      val bounds = boundaryRegion.toLatLngBounds()
      map.setLatLngBoundsForCameraTarget(bounds)
    } ?: map.setLatLngBoundsForCameraTarget(null)
  }

  private fun applyRegion(map: GoogleMap, options: SetRegionOptions, callback: (() -> Unit)? = null) {
    val region = applyPaddingToRegion(options.region, options.padding)
    val bounds = region.toLatLngBounds()
    val cameraUpdate = CameraUpdateFactory.newLatLngBounds(bounds, 0)
    if (options.animateDuration > 0) {
      map.animateCamera(cameraUpdate, options.animateDuration, object : GoogleMap.CancelableCallback {
        override fun onFinish() {
          callback?.invoke()
        }

        override fun onCancel() {
          callback?.invoke()
        }
      })
    } else {
      map.moveCamera(cameraUpdate)
      callback?.invoke()
    }
  }

  private fun applyPaddingToRegion(region: Region, padding: Padding): Region {
    var maxLatitude = region.latitude + region.latitudeDelta / 2
    var minLatitude = region.latitude - region.latitudeDelta / 2
    var maxLongitude = region.longitude + region.longitudeDelta / 2
    var minLongitude = region.longitude - region.longitudeDelta / 2

    val displayDensity = context.resources.displayMetrics.density
    val mapViewHeight = mapView.height / displayDensity
    val mapViewWidth = mapView.width / displayDensity
    val latPerHeight = region.latitudeDelta / (mapViewHeight - padding.top - padding.bottom)
    val lngPerWidth = region.longitudeDelta / (mapViewWidth - padding.left - padding.right)

    maxLatitude += latPerHeight * padding.top
    minLatitude -= latPerHeight * padding.bottom
    maxLongitude += lngPerWidth * padding.right
    minLongitude -= lngPerWidth * padding.left

    return Region(
      latitude = (minLatitude + maxLatitude) / 2,
      longitude = (minLongitude + maxLongitude) / 2,
      latitudeDelta = maxOf(maxLatitude - minLatitude, 0.000001),
      longitudeDelta = maxOf(maxLongitude - minLongitude, 0.000001)
    )
  }
}
