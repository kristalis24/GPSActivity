package de.cleopa.chentschel.gpsactivity.main;

import android.app.Activity;
import android.app.ProgressDialog;
import android.content.Intent;
import android.os.Bundle;
import android.view.View;

import de.cleopa.chentschel.gpsactivity.R;
import de.cleopa.chentschel.gpsactivity.service.GeoPositionsService;

public class GPSActivity extends Activity{//implements GpxParser.GpxParserListener, GpxParserHandler.GpxParserProgressListener{

    private ProgressDialog mProgressDialog = null;

    @Override
    protected void onCreate(Bundle savedInstanceState) {

        super.onCreate(savedInstanceState);
        setContentView(R.layout.gpsactivity);
        setTitle(R.string.title_activity_gps);
        startService(new Intent(this, GeoPositionsService.class));
    }

    @Override
    protected void onDestroy(){
        stopService(new Intent(this, GeoPositionsService.class));
        super.onDestroy();
    }

    public void onClickKarteAnzeigen(final View sfNormal){
        final Intent intent = new Intent(this, KarteAnzeigen.class);
        startActivity(intent);
    }
//
//    @Override
//    public void onGpxParseStarted() {
//        mProgressDialog = ProgressDialog.show(this, "Parsing GPX", "Started");
//    }
//
//    @Override
//    public void onGpxParseCompleted(GPXDocument document) {
//        mProgressDialog.dismiss();
//
//        KarteAnzeigen.mDocument = document;
//
//        Intent intent = new Intent(this, KarteAnzeigen.class);
//        startActivity(intent);
//    }
//
//    @Override
//    public void onGpxParseError(String type, String message, int lineNumber, int columnNumber) {
//        mProgressDialog.dismiss();
//
//        new AlertDialog.Builder(this)
//                .setTitle("Error")
//                .setMessage("An error occurred: " + message)
//                .setPositiveButton(android.R.string.ok, new DialogInterface.OnClickListener() {
//                    public void onClick(DialogInterface dialog, int which) {
//                        dialog.cancel();
//                    }
//                })
//                .show();
//    }
//
//
//
//    @Override
//    public void onGpxNewTrackParsed(int count, GPXTrack track) {
//        mProgressDialog.setMessage("Finished parsing track " + track.getName());
//    }
//
//    @Override
//    public void onGpxNewRouteParsed(int count, GPXRoute track) {
//        mProgressDialog.setMessage("Finished parsing route " + track.getName());
//    }
//
//    @Override
//    public void onGpxNewSegmentParsed(int count, GPXSegment segment) {
//        mProgressDialog.setMessage("Parsing track segment " + count);
//    }
//
//    @Override
//    public void onGpxNewTrackPointParsed(int count, GPXTrackPoint trackPoint) {
//
//    }
//
//    @Override
//    public void onGpxNewRoutePointParsed(int count, GPXRoutePoint routePoint) {
//
//    }
//
//    @Override
//    public void onGpxNewWayPointParsed(int count, GPXWayPoint wayPoint) {
//
//    }


//    /**
//     * Parses the waypoint (wpt tags) data into native objects from a GPX stream.
//     */
//
//    private List<LatLng> loadGpxData(XmlPullParser parser, InputStream gpxIn) throws XmlPullParserException, IOException {
//        // We use a List<> as we need subList for paging later
//        List<LatLng> latLngs = new ArrayList<>();
//        parser.setInput(gpxIn, null);
//        parser.nextTag();
//
//        while (parser.next() != XmlPullParser.END_DOCUMENT) {
//            if (parser.getEventType() != XmlPullParser.START_TAG){
//                continue;
//            }
//
//            if (parser.getName().equals("wpt")){
//                // Save the discovered lat/lon attributes in each <wpt>
//                latLngs.add(new LatLng(
//                        Double.valueOf(parser.getAttributeValue(null, "lat")),
//                        Double.valueOf(parser.getAttributeValue(null, "lon"))));
//            }
//            // Otherwise, skip irrelevant data
//        }
//
//        return latLngs;
//    }
//
//    /**
//     * Snaps the points to their most likely position on roads using the Roads API.
//     */
//    private List<SnappedPoint> snapToRoads(GeoApiContext context) throws Exception{
//        List<SnappedPoint> snappedPoints = new ArrayList<>();
//
//        int offset = 0;
//        while (offset < mCapturedLocations.size()){
//            // Calculate which points to include in this request.
//            // We can't exceed the APIs maximum and we want to ensure
//            // some overlap so the API can infer a good location for
//            // the first few points in each request.
//            if (offset > 0){
//                offset -= PAGINATION_OVERLAB;   // Rewind to include some previous points
//            }
//            int lowerBound = offset;
//            int upperBound = Math.min(offset + PAGE_SIZE_LIMIT, mCapturedLocations.size());
//
//            // Grab the data we need for this page.
//            LatLng[] page = mCapturedLocations
//                    .subList(lowerBound, upperBound)
//                    .toArray(new LatLng[upperBound - lowerBound]);
//            // Perform the request. Because we have interpolate=true, we will get
//            // extra data points between our originally requested path. To ensure
//            // we can concatenate these points, we only start adding once we've hit
//            // the first new point (i.e. skip the overlap).
//            SnappedPoint[] points = RoadsApi.snapToRoads(context, true, page).await();
//            boolean passedOverlap = false;
//            for (SnappedPoint point : points){
//                if (offset == 0 || point.originalIndex >= PAGINATION_OVERLAP - 1){
//                    passedOverlap = true;
//                }
//                if (passedOverlap){
//                    snappedPoints.add(point);
//                }
//            }
//
//            offset = upperBound;
//        }
//
//        return snappedPoints;
//    }
}
