// 두 지점 간의 거리 계산 (미터 단위)
function getDistance(latlng1, latlng2) {
  try {
    var lat1 = latlng1.getLat();
    var lng1 = latlng1.getLng();
    var lat2 = latlng2.getLat();
    var lng2 = latlng2.getLng();

    function deg2rad(deg) {
      return deg * (Math.PI / 180);
    }

    var R = 6371; // 지구 반경 (km)
    var dLat = deg2rad(lat2 - lat1);
    var dLng = deg2rad(lng2 - lng1);
    var a = Math.sin(dLat / 2) * Math.sin(dLat / 2) +
      Math.cos(deg2rad(lat1)) * Math.cos(deg2rad(lat2)) *
      Math.sin(dLng / 2) * Math.sin(dLng / 2);
    var c = 2 * Math.atan2(Math.sqrt(a), Math.sqrt(1 - a));
    var distance = R * c * 1000; // 미터 단위로 변환

    return distance;
  } catch (e) {
    console.error("거리 계산 오류:", e);
    return Infinity; // 오류 발생 시 무한대 거리 반환 (필터링에서 제외되도록)
  }
}