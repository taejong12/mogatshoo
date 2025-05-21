// 여러 키워드로 순차적으로 검색 시도
function searchMultipleKeywords(keywords, index = 0) {
  // 이전 타임아웃 제거
  if (searchTimeout) {
    clearTimeout(searchTimeout);
  }

  // 새 타임아웃 설정 (10초)
  searchTimeout = setTimeout(function() {
    console.log("검색 타임아웃 발생");
    showMessage("검색 시간이 초과되었습니다. 다시 시도해 주세요.", "error");
    showLoading(false);
  }, 10000);

  if (index >= keywords.length) {
    // 모든 키워드로 검색했지만 결과가 없음
    clearTimeout(searchTimeout);
    if (markers.length === 0) {
      showMessage("주변에 탈모 관련 병원을 찾을 수 없습니다. 검색 반경을 넓히거나 다른 키워드로 검색해보세요.", "error");
      if (document.getElementById('hospital-list')) {
        document.getElementById('hospital-list').innerHTML = '<div class="text-center p-4"><p class="text-muted">검색 결과가 없습니다.</p></div>';
      }
    }
    showLoading(false);
    return;
  }

  var position = currentPosition || map.getCenter();
  var radius = 5000; // 기본 반경 5km
  
  // radius 요소가 있으면 해당 값 사용
  if (document.getElementById('radius')) {
    radius = document.getElementById('radius').value;
  }

  console.log("검색 키워드:", keywords[index]);

  var places = new kakao.maps.services.Places();
  places.keywordSearch(keywords[index], function(result, status) {
    console.log("검색 결과 상태:", status, "결과 수:", result ? result.length : 0);

    if (status === kakao.maps.services.Status.OK && result && result.length > 0) {
      // 거리에 따라 결과 필터링
      var filteredResults = result.filter(function(place) {
        var placePosition = new kakao.maps.LatLng(place.y, place.x);
        var distance = getDistance(position, placePosition);
        return distance <= radius;
      });

      console.log("필터링된 결과 수:", filteredResults.length);

      if (filteredResults.length > 0) {
        // 타임아웃 제거
        clearTimeout(searchTimeout);
        // 결과가 있으면 표시
        displaySearchResults(filteredResults);
        if (isFirstSearch) {
          showMessage(filteredResults.length + "개의 " + keywords[index] + " 검색 결과를 찾았습니다.", "info");
          isFirstSearch = false;
        }
        showLoading(false);
      } else {
        // 결과가 없으면 다음 키워드로 검색
        searchMultipleKeywords(keywords, index + 1);
      }
    } else {
      // 검색 실패하면 다음 키워드로 검색
      console.log("키워드 검색 실패 또는 결과 없음, 다음 키워드로 넘어감");
      searchMultipleKeywords(keywords, index + 1);
    }
  }, {
    location: position,
    radius: radius,
    sort: kakao.maps.services.SortBy.DISTANCE
  });
}