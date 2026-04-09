package com.monholm.expounifiedmaps

import android.graphics.drawable.Drawable
import com.google.android.gms.maps.model.LatLng as GoogleLatLng
import com.google.android.gms.maps.model.LatLngBounds
import expo.modules.kotlin.records.Field
import expo.modules.kotlin.records.Record
import expo.modules.kotlin.sharedobjects.SharedRef

data class LatLng(
  @Field val latitude: Double = 0.0,
  @Field val longitude: Double = 0.0
) : Record {
  fun toGoogleLatLng(): GoogleLatLng {
    return GoogleLatLng(latitude, longitude)
  }
}

data class Region(
  @Field val latitude: Double = 0.0,
  @Field val longitude: Double = 0.0,
  @Field val latitudeDelta: Double = 0.0,
  @Field val longitudeDelta: Double = 0.0
) : Record {
  fun toLatLngBounds(): LatLngBounds {
      val halfLatitudeDelta = latitudeDelta / 2
      val halfLongitudeDelta = longitudeDelta / 2

      val northeastLatitude = latitude + halfLatitudeDelta
      val northeastLongitude = longitude + halfLongitudeDelta

      val southWestLatitude = latitude - halfLatitudeDelta
      val southWestLongitude = longitude - halfLongitudeDelta

      return LatLngBounds.builder()
        .include(GoogleLatLng(northeastLatitude, northeastLongitude))
        .include(GoogleLatLng(southWestLatitude, southWestLongitude))
        .build()
  }
}

class SetRegionOptions : Record {
  @Field val region: Region = Region()

  @Field val animateDuration: Int = 0
}

class Padding : Record {
  @Field val top: Double = 0.0
  @Field val bottom: Double = 0.0
  @Field val left: Double = 0.0
  @Field val right: Double = 0.0
}

class FitToCoordinatesOptions : Record {
  @Field val coordinates: Array<LatLng> = emptyArray()
  @Field val padding: Padding = Padding()
  @Field val animateDuration: Int = 0
}

class Marker : Record {
  @Field val id: String = ""
  @Field val coordinate: LatLng = LatLng()
  @Field val icon: SharedRef<Drawable>? = null
}
