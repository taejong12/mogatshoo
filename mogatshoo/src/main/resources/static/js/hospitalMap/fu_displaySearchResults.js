// 검색 결과 표시
function displaySearchResults(places) {
  try {
    var bounds = new kakao.maps.LatLngBounds();
    var hospitalList = document.getElementById('hospital-list');
    
    if (hospitalList) {
      hospitalList.innerHTML = '';
    } else {
      console.warn("hospital-list 요소를 찾을 수 없습니다");
    }

    console.log("표시할 장소 수:", places.length);

    for (var i = 0; i < places.length; i++) {
      var place = places[i];
      try {
        displayMarker(place);

        var placePosition = new kakao.maps.LatLng(place.y, place.x);
        bounds.extend(placePosition);

        // 병원 목록이 있을 경우만 항목 추가
        if (hospitalList) {
          // 병원 목록 아이템 생성
          var item = document.createElement('div');
          item.className = 'hospital-item';
          item.innerHTML = '<div class="hospital-name">' + place.place_name + '</div>' +
            '<div class="hospital-address">' + place.address_name + '</div>' +
            '<div class="hospital-phone">' + (place.phone || '전화번호 없음') + '</div>';

          // 목록 항목 클릭 시 해당 마커로 이동
          (function(position, index) {
            item.onclick = function() {
              map.setCenter(position);
              map.setLevel(3);

              // 인포윈도우 열기
              closeAllInfowindows();
              if (infowindows[index]) {
                infowindows[index].open(map, markers[index]);
              }
            };
          })(placePosition, i);

          hospitalList.appendChild(item);
        }
      } catch (e) {
        console.error("장소 표시 오류:", e, "장소:", place);
      }
    }

    // 검색된 장소 위치를 기준으로 지도 범위 재설정
    if (places.length > 0) {
      map.setBounds(bounds);
    }
  } catch (e) {
    console.error("검색 결과 표시 오류:", e);
    showMessage("검색 결과를 표시하는 중 오류가 발생했습니다.", "error");
  }
}
