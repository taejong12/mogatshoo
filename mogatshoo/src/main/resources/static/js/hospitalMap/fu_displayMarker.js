// 마커 표시
function displayMarker(place) {
  try {
    var marker = new kakao.maps.Marker({
      map: map,
      position: new kakao.maps.LatLng(place.y, place.x)
    });

    markers.push(marker);

    var infoContent = '<div style="padding:10px;min-width:200px;">' +
      '<h5 style="margin-top:5px;margin-bottom:5px;">' + place.place_name + '</h5>' +
      '<div style="font-size:13px;color:#666;margin-bottom:5px;">' + place.address_name + '</div>';

    if (place.phone) {
      infoContent += '<div style="font-size:13px;color:#666;margin-bottom:5px;">☎ ' + place.phone + '</div>';
    }

    if (place.place_url) {
      infoContent += '<a href="' + place.place_url + '" target="_blank" style="color:#007bff;font-size:13px;">상세 정보 보기</a>';
    }

    infoContent += '</div>';

    var infowindow = new kakao.maps.InfoWindow({
      content: infoContent
    });

    infowindows.push(infowindow);

    kakao.maps.event.addListener(marker, 'click', function() {
      closeAllInfowindows();
      infowindow.open(map, marker);
    });
  } catch (e) {
    console.error("마커 표시 오류:", e, "장소:", place);
  }
}

// 모든 마커 제거
function removeAllMarkers() {
  for (var i = 0; i < markers.length; i++) {
    markers[i].setMap(null);
  }
  markers = [];

  closeAllInfowindows();
}

// 모든 인포윈도우 닫기
function closeAllInfowindows() {
  for (var i = 0; i < infowindows.length; i++) {
    if (infowindows[i]) {
      infowindows[i].close();
    }
  }
}