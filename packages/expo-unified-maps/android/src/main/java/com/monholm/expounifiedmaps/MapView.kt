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
import com.google.android.gms.maps.model.LatLngBounds
import com.google.android.gms.maps.model.MarkerOptions
import com.google.maps.android.collections.MarkerManager
import expo.modules.kotlin.viewevent.EventDispatcher

class MapView(context: Context, appContext: AppContext) : ExpoView(context, appContext) {
  val onMarkerClick by EventDispatcher()
  internal val mapView = com.google.android.gms.maps.MapView(context)
  private var googleMap: GoogleMap? = null
  private var markerManager: MarkerManager? = null
  private var defaultMarkerCollection: MarkerManager.Collection? = null

  // Pending settings to be applied once the map is ready
  // Disable toolbar to align with iOS behavior
  private val toolbarEnabled: Boolean = false
  private var showCompass: Boolean? = null
  private var zoomEnabled: Boolean? = null
  private var scrollEnabled: Boolean? = null
  private var rotateEnabled: Boolean? = null
  private var pitchEnabled: Boolean? = null
  private var boundary: Region? = null
  private var mapPadding: Padding? = null
  private var pendingMarkers: Array<Marker>? = null
  private val currentMarkers = mutableMapOf<String, com.google.android.gms.maps.model.Marker>()

  init {
    addView(mapView)
    mapView.onCreate(null)
    mapView.onResume()
    mapView.getMapAsync {
      this.googleMap = it
      val markerManager = MarkerManager(it)
      this.markerManager = markerManager
      val defaultMarkerCollection = markerManager.newCollection()
      this.defaultMarkerCollection = defaultMarkerCollection
      applyPendingSettings(it, defaultMarkerCollection)
    }
  }

  fun setShowCompass(enabled: Boolean?) {
    enabled?.let { isEnabled ->
      showCompass = isEnabled
      googleMap?.let { map ->
        map.getUiSettings().setCompassEnabled(isEnabled)
      }
    }
  }

  fun setZoomEnabled(enabled: Boolean?) {
    enabled?.let { isEnabled ->
      zoomEnabled = isEnabled
      googleMap?.let { map ->
        map.getUiSettings().setZoomGesturesEnabled(isEnabled)
      }
    }
  }

  fun setScrollEnabled(enabled: Boolean?) {
    enabled?.let { isEnabled ->
      scrollEnabled = isEnabled
      googleMap?.let { map ->
        map.getUiSettings().setScrollGesturesEnabled(isEnabled)
      }
    }
  }

  fun setRotateEnabled(enabled: Boolean?) {
    enabled?.let { isEnabled ->
      rotateEnabled = isEnabled
      googleMap?.let { map ->
        map.getUiSettings().setRotateGesturesEnabled(isEnabled)
      }
    }
  }

  fun setPitchEnabled(enabled: Boolean?) {
    enabled?.let { isEnabled ->
      pitchEnabled = isEnabled
      googleMap?.let { map ->
        map.getUiSettings().setTiltGesturesEnabled(isEnabled)
      }
    }
  }

  fun fitToCoordinates(options: FitToCoordinatesOptions, promise: Promise) {
    googleMap?.let { map ->
      val coordinates = options.coordinates

      var maxLatitude = -90.0
      var minLatitude = 90.0
      var maxLongitude = -180.0
      var minLongitude = 180.0

      for (coord in coordinates) {
        maxLatitude = maxOf(maxLatitude, coord.latitude)
        minLatitude = minOf(minLatitude, coord.latitude)
        maxLongitude = maxOf(maxLongitude, coord.longitude)
        minLongitude = minOf(minLongitude, coord.longitude)
      }

      val padding = options.padding
      val displayDensity = context.resources.displayMetrics.density
      val mapViewHeight = mapView.height / displayDensity
      val mapViewWidth = mapView.width / displayDensity
      val latitudeDelta = maxLatitude - minLatitude
      val longitudeDelta = maxLongitude - minLongitude
      val latPerHeight = latitudeDelta / (mapViewHeight - padding.top - padding.bottom)
      val lngPerWidth = longitudeDelta / (mapViewWidth - padding.left - padding.right)

      maxLatitude += latPerHeight * padding.top
      minLatitude -= latPerHeight * padding.bottom
      maxLongitude += lngPerWidth * padding.right
      minLongitude -= lngPerWidth * padding.left

      val finalLatDelta = maxOf(maxLatitude - minLatitude, 0.000001)
      val finalLngDelta = maxOf(maxLongitude - minLongitude, 0.000001)
      val centerLat = (minLatitude + maxLatitude) / 2
      val centerLng = (minLongitude + maxLongitude) / 2

      val bounds = LatLngBounds.builder()
        .include(com.google.android.gms.maps.model.LatLng(centerLat + finalLatDelta / 2, centerLng + finalLngDelta / 2))
        .include(com.google.android.gms.maps.model.LatLng(centerLat - finalLatDelta / 2, centerLng - finalLngDelta / 2))
        .build()

      val cameraUpdate = CameraUpdateFactory.newLatLngBounds(bounds, 0)
      if (options.animateDuration > 0) {
        map.animateCamera(cameraUpdate, options.animateDuration, object : GoogleMap.CancelableCallback {
          override fun onFinish() { promise.resolve(null) }
          override fun onCancel() { promise.resolve(null) }
        })
      } else {
        map.moveCamera(cameraUpdate)
        promise.resolve(null)
      }
    } ?: promise.reject("MAP_NOT_READY", "GoogleMap is not ready yet", null)
  }

  fun setRegion(options: SetRegionOptions, promise: Promise) {
    googleMap?.let { map ->
      applyRegion(map, options) {
        promise.resolve(null)
      }
    } ?: promise.reject("MAP_NOT_READY", "GoogleMap is not ready yet", null)
  }

  fun setMapPadding(padding: Padding?) {
    mapPadding = padding
    googleMap?.let { applyMapPadding(it, padding) }
  }

  fun setBoundary(region: Region?) {
    boundary = region
    googleMap?.let { map ->
      applyBoundary(map, region)
    }
  }

  fun setMarkers(markers: Array<Marker>?) {
    pendingMarkers = markers
    applyMarkers()
  }

  private fun applyMarkers() {
    val collection = defaultMarkerCollection ?: return
    val incoming = pendingMarkers ?: emptyArray()
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

  private fun applyMapPadding(map: GoogleMap, padding: Padding?) {
    if (padding != null) {
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
  }

  private fun applyBoundary(map: GoogleMap, region: Region?) {
    region?.let { boundaryRegion ->
      val bounds = boundaryRegion.toLatLngBounds()
      map.setLatLngBoundsForCameraTarget(bounds)
    } ?: map.setLatLngBoundsForCameraTarget(null)
  }

  private fun applyPendingSettings(map: GoogleMap, defaultMarkerCollection: MarkerManager.Collection) {
    map.getUiSettings().setMapToolbarEnabled(toolbarEnabled)
    
    showCompass?.let { isEnabled ->
      map.getUiSettings().setCompassEnabled(isEnabled)
    }

    zoomEnabled?.let { isEnabled ->
      map.getUiSettings().setZoomGesturesEnabled(isEnabled)
    }
    scrollEnabled?.let { isEnabled ->
      map.getUiSettings().setScrollGesturesEnabled(isEnabled)
    }
    rotateEnabled?.let { isEnabled ->
      map.getUiSettings().setRotateGesturesEnabled(isEnabled)
    }
    pitchEnabled?.let { isEnabled ->
      map.getUiSettings().setTiltGesturesEnabled(isEnabled)
    }

    applyMapPadding(map, mapPadding)

    applyMarkers()

    applyBoundary(map, boundary)

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
  }

  private fun applyRegion(map: GoogleMap, options: SetRegionOptions, callback: (() -> Unit)? = null) {
    val bounds = options.region.toLatLngBounds()
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
}
