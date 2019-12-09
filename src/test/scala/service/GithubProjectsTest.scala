package service

import java.time.Instant

import cats.effect.IO
import fs2.Stream
import io.circe
import io.circe.Json
import io.circe.literal._
import model.{High, Low, Medium, Todo}
import org.http4s.circe._
import org.http4s.dsl.io._
import org.http4s.{Request, Response, Status, Uri}
import org.scalamock.scalatest.MockFactory
import org.scalatest.{Matchers, WordSpec}
import org.scalatest._
import io.circe.parser.decode
import repository.Github.{Author, Commit, Payload, Repo, Tree, UserActivityEvent}
import io.circe.generic.auto._

class GithubProjectsTest extends WordSpec with MockFactory with Matchers {

  import repository.Github.Author.decodeInstant

  "GithubProjects" should {
    "decode an author" in {

      val decodeResult: Either[circe.Error, Author] = decode[Author](
      """
      {
        "name": "swoogles",
        "email": "bill.frasure@gmail.com",
        "date": "2019-12-03T10:58:59Z"
      }
      """
      )
      println(decodeResult)
    }
    "create a branch" in {
      val id = 1
      val todo = Todo(None, "my todo", Low)
      //      val response = serve(Request[IO](POST, uri("/todos")).withBody(createJson).unsafeRunSync())
      //      response.status shouldBe Status.Created
      val parseResult =
      decode[Tree](
        """
{
  "sha": "42024c49fe1b7269ff22b80d8e8477562a44b870",
  "node_id": "MDY6Q29tbWl0ODk4MzE0MDM6NDIwMjRjNDlmZTFiNzI2OWZmMjJiODBkOGU4NDc3NTYyYTQ0Yjg3MA==",
  "commit": {
    "author": {
      "name": "swoogles",
      "email": "bill.frasure@gmail.com",
      "date": "2019-12-03T10:58:59Z"
    },
    "committer": {
      "name": "swoogles",
      "email": "bill.frasure@gmail.com",
      "date": "2019-12-03T10:58:59Z"
    },
    "message": "Better styling & sample scenes",
    "tree": {
      "sha": "5b321eb9725521ca2fa3ccf2397a11b3582e0999",
      "url": "https://api.github.com/repos/swoogles/TrafficSimulation/git/trees/5b321eb9725521ca2fa3ccf2397a11b3582e0999"
    },
    "url": "https://api.github.com/repos/swoogles/TrafficSimulation/git/commits/42024c49fe1b7269ff22b80d8e8477562a44b870",
    "comment_count": 0,
    "verification": {
      "verified": false,
      "reason": "unsigned",
      "signature": null,
      "payload": null
    }
  },
  "url": "https://api.github.com/repos/swoogles/TrafficSimulation/commits/42024c49fe1b7269ff22b80d8e8477562a44b870",
  "html_url": "https://github.com/swoogles/TrafficSimulation/commit/42024c49fe1b7269ff22b80d8e8477562a44b870",
  "comments_url": "https://api.github.com/repos/swoogles/TrafficSimulation/commits/42024c49fe1b7269ff22b80d8e8477562a44b870/comments",
  "author": {
    "login": "swoogles",
    "id": 2054940,
    "node_id": "MDQ6VXNlcjIwNTQ5NDA=",
    "avatar_url": "https://avatars2.githubusercontent.com/u/2054940?v=4",
    "gravatar_id": "",
    "url": "https://api.github.com/users/swoogles",
    "html_url": "https://github.com/swoogles",
    "followers_url": "https://api.github.com/users/swoogles/followers",
    "following_url": "https://api.github.com/users/swoogles/following{/other_user}",
    "gists_url": "https://api.github.com/users/swoogles/gists{/gist_id}",
    "starred_url": "https://api.github.com/users/swoogles/starred{/owner}{/repo}",
    "subscriptions_url": "https://api.github.com/users/swoogles/subscriptions",
    "organizations_url": "https://api.github.com/users/swoogles/orgs",
    "repos_url": "https://api.github.com/users/swoogles/repos",
    "events_url": "https://api.github.com/users/swoogles/events{/privacy}",
    "received_events_url": "https://api.github.com/users/swoogles/received_events",
    "type": "User",
    "site_admin": false
  },
  "committer": {
    "login": "swoogles",
    "id": 2054940,
    "node_id": "MDQ6VXNlcjIwNTQ5NDA=",
    "avatar_url": "https://avatars2.githubusercontent.com/u/2054940?v=4",
    "gravatar_id": "",
    "url": "https://api.github.com/users/swoogles",
    "html_url": "https://github.com/swoogles",
    "followers_url": "https://api.github.com/users/swoogles/followers",
    "following_url": "https://api.github.com/users/swoogles/following{/other_user}",
    "gists_url": "https://api.github.com/users/swoogles/gists{/gist_id}",
    "starred_url": "https://api.github.com/users/swoogles/starred{/owner}{/repo}",
    "subscriptions_url": "https://api.github.com/users/swoogles/subscriptions",
    "organizations_url": "https://api.github.com/users/swoogles/orgs",
    "repos_url": "https://api.github.com/users/swoogles/repos",
    "events_url": "https://api.github.com/users/swoogles/events{/privacy}",
    "received_events_url": "https://api.github.com/users/swoogles/received_events",
    "type": "User",
    "site_admin": false
  },
  "parents": [
    {
      "sha": "b7867927838450bfe1c9d5bfb0365fe3e2bfbeaf",
      "url": "https://api.github.com/repos/swoogles/TrafficSimulation/commits/b7867927838450bfe1c9d5bfb0365fe3e2bfbeaf",
      "html_url": "https://github.com/swoogles/TrafficSimulation/commit/b7867927838450bfe1c9d5bfb0365fe3e2bfbeaf"
    }
  ],
  "stats": {
    "total": 52,
    "additions": 28,
    "deletions": 24
  },
  "files": [
    {
      "sha": "91bcdb0f439098e9076bf26ce3d700e72e18ae03",
      "filename": "index.html",
      "status": "modified",
      "additions": 1,
      "deletions": 1,
      "changes": 2,
      "blob_url": "https://github.com/swoogles/TrafficSimulation/blob/42024c49fe1b7269ff22b80d8e8477562a44b870/index.html",
      "raw_url": "https://github.com/swoogles/TrafficSimulation/raw/42024c49fe1b7269ff22b80d8e8477562a44b870/index.html",
      "contents_url": "https://api.github.com/repos/swoogles/TrafficSimulation/contents/index.html?ref=42024c49fe1b7269ff22b80d8e8477562a44b870",
      "patch": "@@ -8,7 +8,7 @@\n </head>\n <body>\n <!-- Include Scala.js compiled code -->\n-<p> Page that should be displaying traffic...</p>\n+<h2 style=\"text-align: center\"> Embouteillage Traffic Simulator </h2>\n <hr>\n <div id=\"svg-container\">\n </div>"
    },
    {
      "sha": "cb1f98e8b162c5142ac395679c5bf05f2ae472c4",
      "filename": "src/main/scala/com/billding/Client.scala",
      "status": "modified",
      "additions": 1,
      "deletions": 5,
      "changes": 6,
      "blob_url": "https://github.com/swoogles/TrafficSimulation/blob/42024c49fe1b7269ff22b80d8e8477562a44b870/src/main/scala/com/billding/Client.scala",
      "raw_url": "https://github.com/swoogles/TrafficSimulation/raw/42024c49fe1b7269ff22b80d8e8477562a44b870/src/main/scala/com/billding/Client.scala",
      "contents_url": "https://api.github.com/repos/swoogles/TrafficSimulation/contents/src/main/scala/com/billding/Client.scala?ref=42024c49fe1b7269ff22b80d8e8477562a44b870",
      "patch": "@@ -89,10 +89,6 @@ object Client {\n     println(\"DT: \" + DT)\n     val controlsContainer = dom.document.getElementById(\"controls-container\")\n     controlsContainer.appendChild(controlElements.createLayout())\n-\n-    val canvasHeight = 300 // TODO Ugh. I don't understand how this ripples through my program :(\n-    val canvasWidth = 1500\n-\n     val svgContainerAttempt: Option[Element] = Option(dom.document.getElementById(\"svg-container\"))\n     svgContainerAttempt match {\n       case Some(svgContainer) => setupSvgAndButtonResponses(svgContainer)\n@@ -106,7 +102,7 @@ object Client {\n     println(\"svgContainer height: \" + svgContainer.clientHeight)\n     println(\"svgContainer width: \" + svgContainer.clientWidth)\n     val windowLocal: Rx[Window] = Rx {\n-      new Window(sceneVar(), svgContainer.clientWidth / 5, svgContainer.clientWidth)\n+      new Window(sceneVar(), svgContainer.clientWidth / 8, svgContainer.clientWidth)\n     }\n \n     windowLocal.trigger {"
    },
    {
      "sha": "2821cc45a17bcc0f531585a909559bb99b888cbd",
      "filename": "src/main/scala/com/billding/ControlElements.scala",
      "status": "modified",
      "additions": 1,
      "deletions": 1,
      "changes": 2,
      "blob_url": "https://github.com/swoogles/TrafficSimulation/blob/42024c49fe1b7269ff22b80d8e8477562a44b870/src/main/scala/com/billding/ControlElements.scala",
      "raw_url": "https://github.com/swoogles/TrafficSimulation/raw/42024c49fe1b7269ff22b80d8e8477562a44b870/src/main/scala/com/billding/ControlElements.scala",
      "contents_url": "https://api.github.com/repos/swoogles/TrafficSimulation/contents/src/main/scala/com/billding/ControlElements.scala?ref=42024c49fe1b7269ff22b80d8e8477562a44b870",
      "patch": "@@ -35,7 +35,7 @@ case class ControlElements(buttonBehaviors: ButtonBehaviors) {\n \n        */\n //      dangerButton(\"Disrupt the flow\", buttonBehaviors.toggleDisrupt),\n-      dangerButton(\"Disrupt the flow\", buttonBehaviors.toggleDisruptExisting)\n+      dangerButton(\"Make 1 car brake\", buttonBehaviors.toggleDisruptExisting)\n     ).render\n \n   val sliders ="
    },
    {
      "sha": "24bcb9ee73d9fee6d2a6d69827619d5b44c8b5c1",
      "filename": "src/main/scala/com/billding/OutterStyles.scala",
      "status": "modified",
      "additions": 8,
      "deletions": 0,
      "changes": 8,
      "blob_url": "https://github.com/swoogles/TrafficSimulation/blob/42024c49fe1b7269ff22b80d8e8477562a44b870/src/main/scala/com/billding/OutterStyles.scala",
      "raw_url": "https://github.com/swoogles/TrafficSimulation/raw/42024c49fe1b7269ff22b80d8e8477562a44b870/src/main/scala/com/billding/OutterStyles.scala",
      "contents_url": "https://api.github.com/repos/swoogles/TrafficSimulation/contents/src/main/scala/com/billding/OutterStyles.scala?ref=42024c49fe1b7269ff22b80d8e8477562a44b870",
      "patch": "@@ -15,6 +15,14 @@ object OutterStyles {\n   object TrafficStyles extends StyleSheet.Inline {\n     import dsl._\n \n+    /* Unsuccessful attempt at styling the header\n+    val headerStyling = style(\n+      addClassName(\"header-title\"),\n+        textAlign.center\n+    )\n+\n+     */\n+\n     val blue: Color = c\"#0000FF\"\n     val green = c\"#00FF00\"\n "
    },
    {
      "sha": "0a75f22b07ecc78f950684e19f07925c9803741c",
      "filename": "src/main/scala/com/billding/SampleSceneCreation.scala",
      "status": "modified",
      "additions": 8,
      "deletions": 8,
      "changes": 16,
      "blob_url": "https://github.com/swoogles/TrafficSimulation/blob/42024c49fe1b7269ff22b80d8e8477562a44b870/src/main/scala/com/billding/SampleSceneCreation.scala",
      "raw_url": "https://github.com/swoogles/TrafficSimulation/raw/42024c49fe1b7269ff22b80d8e8477562a44b870/src/main/scala/com/billding/SampleSceneCreation.scala",
      "contents_url": "https://api.github.com/repos/swoogles/TrafficSimulation/contents/src/main/scala/com/billding/SampleSceneCreation.scala?ref=42024c49fe1b7269ff22b80d8e8477562a44b870",
      "patch": "@@ -30,13 +30,13 @@ class SampleSceneCreation(endingSpatial: Spatial)(implicit val DT: Time) {\n     NamedScene(\n       \"group encountering a stopped vehicle\",\n       createWithVehicles(\n-        Seconds(3.5),\n+        Seconds(3),\n         List(\n-          simplerVehicle(100, 0.1),\n-          simplerVehicle(60, 100),\n-          simplerVehicle(45, 100),\n-          simplerVehicle(30, 100),\n-          simplerVehicle(15, 100)\n+          simplerVehicle(90, 0.1),\n+          simplerVehicle(55, 100),\n+          simplerVehicle(40, 100),\n+          simplerVehicle(25, 100),\n+          simplerVehicle(10, 100)\n         )\n       )\n     )\n@@ -63,7 +63,7 @@ class SampleSceneCreation(endingSpatial: Spatial)(implicit val DT: Time) {\n     NamedScene(\n       \"multiple stopped groups getting back up to speed\",\n       createWithVehicles(\n-        Seconds(3),\n+        Seconds(4),\n         List(\n           simplerVehicle(125, 0),\n           simplerVehicle(120, 0),\n@@ -98,7 +98,7 @@ class SampleSceneCreation(endingSpatial: Spatial)(implicit val DT: Time) {\n \n   private def createWithVehicles(sourceTiming: Time, vehicles: List[PilotedVehicle]): Scene = {\n \n-    val speedLimit: Velocity = KilometersPerHour(65)\n+    val speedLimit: Velocity = KilometersPerHour(45) // TODO Connect this to Car Speed Control.\n     val originSpatial = Spatial((0, 0, 0, Kilometers))\n     val endingSpatial = Spatial((0.5, 0, 0, Kilometers))\n     val canvasDimensions: (Length, Length) = (Kilometers(.25), Kilometers(.5))"
    },
    {
      "sha": "3c02a26833d16a365b98a2847d9d6e3e5dcb1694",
      "filename": "src/main/scala/com/billding/svgRendering/SpatialCanvas.scala",
      "status": "modified",
      "additions": 1,
      "deletions": 1,
      "changes": 2,
      "blob_url": "https://github.com/swoogles/TrafficSimulation/blob/42024c49fe1b7269ff22b80d8e8477562a44b870/src/main/scala/com/billding/svgRendering/SpatialCanvas.scala",
      "raw_url": "https://github.com/swoogles/TrafficSimulation/raw/42024c49fe1b7269ff22b80d8e8477562a44b870/src/main/scala/com/billding/svgRendering/SpatialCanvas.scala",
      "contents_url": "https://api.github.com/repos/swoogles/TrafficSimulation/contents/src/main/scala/com/billding/svgRendering/SpatialCanvas.scala?ref=42024c49fe1b7269ff22b80d8e8477562a44b870",
      "patch": "@@ -17,6 +17,6 @@ case class SpatialCanvas(\n                           pixelWidth: Int\n ) {\n   // Still not thrilled about this arbitrary multiplication\n-  val heightDistancePerPixel: Distance = height / (pixelHeight * 3)\n+  val heightDistancePerPixel: Distance = height / (pixelHeight * 5)\n   val widthDistancePerPixel: Distance = width / (pixelWidth * 3)\n }"
    },
    {
      "sha": "be63cb387731841f37933400b13fb6ad83f21fef",
      "filename": "target/scala-2.12/traffic-fastopt.js",
      "status": "modified",
      "additions": 8,
      "deletions": 8,
      "changes": 16,
      "blob_url": "https://github.com/swoogles/TrafficSimulation/blob/42024c49fe1b7269ff22b80d8e8477562a44b870/target/scala-2.12/traffic-fastopt.js",
      "raw_url": "https://github.com/swoogles/TrafficSimulation/raw/42024c49fe1b7269ff22b80d8e8477562a44b870/target/scala-2.12/traffic-fastopt.js",
      "contents_url": "https://api.github.com/repos/swoogles/TrafficSimulation/contents/target/scala-2.12/traffic-fastopt.js?ref=42024c49fe1b7269ff22b80d8e8477562a44b870",
      "patch": "@@ -3317,7 +3317,7 @@ $c_Lcom_billding_Client$.prototype.setupSvgAndButtonResponses__Lorg_scalajs_dom_\n       var rxOwnerCtx$macro$2 = $as_Lrx_Ctx$Owner(rxOwnerCtx$macro$2$2);\n       var rxDataCtx$macro$1 = $as_Lrx_Ctx$Data(rxDataCtx$macro$1$2);\n       var this$7 = $m_Lcom_billding_Client$().sceneVar$1;\n-      return new $c_Lcom_billding_Window().init___Lcom_billding_traffic_Scene__I__I__Lrx_Ctx$Owner__Lcom_billding_physics_SpatialFor($as_Lcom_billding_traffic_Scene($f_Lrx_Rx__apply__Lrx_Ctx$Data__O(this$7, rxDataCtx$macro$1)), (($uI(svgContainer$1.clientWidth) / 5) | 0), $uI(svgContainer$1.clientWidth), rxOwnerCtx$macro$2, $m_Lcom_billding_Client$().spatialForPilotedVehicle$1)\n+      return new $c_Lcom_billding_Window().init___Lcom_billding_traffic_Scene__I__I__Lrx_Ctx$Owner__Lcom_billding_physics_SpatialFor($as_Lcom_billding_traffic_Scene($f_Lrx_Rx__apply__Lrx_Ctx$Data__O(this$7, rxDataCtx$macro$1)), (($uI(svgContainer$1.clientWidth) / 8) | 0), $uI(svgContainer$1.clientWidth), rxOwnerCtx$macro$2, $m_Lcom_billding_Client$().spatialForPilotedVehicle$1)\n     })\n   })(this, svgContainer)), $m_Lscaladget_tools_JsRxTags$().ctx$1);\n   var thunk = new $c_sjsr_AnonFunction0().init___sjs_js_Function0((function(this$2$1, svgContainer$2, windowLocal$1) {\n@@ -3454,10 +3454,10 @@ $c_Lcom_billding_SampleSceneCreation.prototype.init___Lcom_billding_physics_Spat\n   };\n   this.emptyScene$1 = new $c_Lcom_billding_NamedScene().init___T__Lcom_billding_traffic_Scene(\"Empty Scene\", this.createWithVehicles__p1__Lsquants_time_Time__sci_List__Lcom_billding_traffic_Scene(jsx$1, result));\n   var this$6 = $m_Lsquants_time_Seconds$();\n-  var num$1 = $m_s_math_Numeric$DoubleIsFractional$();\n-  var jsx$2 = $m_Lsquants_time_Time$().apply__O__Lsquants_time_TimeUnit__s_math_Numeric__Lsquants_time_Time(3.5, this$6, num$1);\n+  var num$1 = $m_s_math_Numeric$IntIsIntegral$();\n+  var jsx$2 = $m_Lsquants_time_Time$().apply__O__Lsquants_time_TimeUnit__s_math_Numeric__Lsquants_time_Time(3, this$6, num$1);\n   $m_sci_List$();\n-  var array$1 = [this.simplerVehicle__p1__D__D__Lcom_billding_traffic_PilotedVehicle(100.0, 0.1), this.simplerVehicle__p1__D__D__Lcom_billding_traffic_PilotedVehicle(60.0, 100.0), this.simplerVehicle__p1__D__D__Lcom_billding_traffic_PilotedVehicle(45.0, 100.0), this.simplerVehicle__p1__D__D__Lcom_billding_traffic_PilotedVehicle(30.0, 100.0), this.simplerVehicle__p1__D__D__Lcom_billding_traffic_PilotedVehicle(15.0, 100.0)];\n+  var array$1 = [this.simplerVehicle__p1__D__D__Lcom_billding_traffic_PilotedVehicle(90.0, 0.1), this.simplerVehicle__p1__D__D__Lcom_billding_traffic_PilotedVehicle(55.0, 100.0), this.simplerVehicle__p1__D__D__Lcom_billding_traffic_PilotedVehicle(40.0, 100.0), this.simplerVehicle__p1__D__D__Lcom_billding_traffic_PilotedVehicle(25.0, 100.0), this.simplerVehicle__p1__D__D__Lcom_billding_traffic_PilotedVehicle(10.0, 100.0)];\n   var i$1 = (((-1) + $uI(array$1.length)) | 0);\n   var result$1 = $m_sci_Nil$();\n   while ((i$1 >= 0)) {\n@@ -3485,7 +3485,7 @@ $c_Lcom_billding_SampleSceneCreation.prototype.init___Lcom_billding_physics_Spat\n   this.scene2$1 = new $c_Lcom_billding_NamedScene().init___T__Lcom_billding_traffic_Scene(\"stopped group getting back up to speed\", this.createWithVehicles__p1__Lsquants_time_Time__sci_List__Lcom_billding_traffic_Scene(jsx$3, result$2));\n   var this$16 = $m_Lsquants_time_Seconds$();\n   var num$3 = $m_s_math_Numeric$IntIsIntegral$();\n-  var jsx$4 = $m_Lsquants_time_Time$().apply__O__Lsquants_time_TimeUnit__s_math_Numeric__Lsquants_time_Time(3, this$16, num$3);\n+  var jsx$4 = $m_Lsquants_time_Time$().apply__O__Lsquants_time_TimeUnit__s_math_Numeric__Lsquants_time_Time(4, this$16, num$3);\n   $m_sci_List$();\n   var array$3 = [this.simplerVehicle__p1__D__D__Lcom_billding_traffic_PilotedVehicle(125.0, 0.0), this.simplerVehicle__p1__D__D__Lcom_billding_traffic_PilotedVehicle(120.0, 0.0), this.simplerVehicle__p1__D__D__Lcom_billding_traffic_PilotedVehicle(115.0, 0.0), this.simplerVehicle__p1__D__D__Lcom_billding_traffic_PilotedVehicle(110.0, 0.0), this.simplerVehicle__p1__D__D__Lcom_billding_traffic_PilotedVehicle(105.0, 0.0), this.simplerVehicle__p1__D__D__Lcom_billding_traffic_PilotedVehicle(100.0, 0.0), this.simplerVehicle__p1__D__D__Lcom_billding_traffic_PilotedVehicle(95.0, 0.0), this.simplerVehicle__p1__D__D__Lcom_billding_traffic_PilotedVehicle(90.0, 0.0), this.simplerVehicle__p1__D__D__Lcom_billding_traffic_PilotedVehicle(60.0, 0.0), this.simplerVehicle__p1__D__D__Lcom_billding_traffic_PilotedVehicle(55.0, 0.0), this.simplerVehicle__p1__D__D__Lcom_billding_traffic_PilotedVehicle(50.0, 0.0), this.simplerVehicle__p1__D__D__Lcom_billding_traffic_PilotedVehicle(45.0, 0.0), this.simplerVehicle__p1__D__D__Lcom_billding_traffic_PilotedVehicle(40.0, 0.0), this.simplerVehicle__p1__D__D__Lcom_billding_traffic_PilotedVehicle(35.0, 0.0), this.simplerVehicle__p1__D__D__Lcom_billding_traffic_PilotedVehicle(30.0, 0.0)];\n   var i$3 = (((-1) + $uI(array$3.length)) | 0);\n@@ -3518,7 +3518,7 @@ $c_Lcom_billding_SampleSceneCreation.prototype.init___Lcom_billding_physics_Spat\n $c_Lcom_billding_SampleSceneCreation.prototype.createWithVehicles__p1__Lsquants_time_Time__sci_List__Lcom_billding_traffic_Scene = (function(sourceTiming, vehicles) {\n   var this$1 = $m_Lsquants_motion_KilometersPerHour$();\n   var num = $m_s_math_Numeric$IntIsIntegral$();\n-  var speedLimit = $m_Lsquants_motion_Velocity$().apply__O__Lsquants_motion_VelocityUnit__s_math_Numeric__Lsquants_motion_Velocity(65, this$1, num);\n+  var speedLimit = $m_Lsquants_motion_Velocity$().apply__O__Lsquants_motion_VelocityUnit__s_math_Numeric__Lsquants_motion_Velocity(45, this$1, num);\n   var this$2 = $m_Lcom_billding_physics_Spatial$();\n   var _4 = $m_Lsquants_space_Kilometers$();\n   var vIn = this$2.ZERO$undVELOCITY$1;\n@@ -30342,7 +30342,7 @@ $c_Lcom_billding_ControlElements.prototype.init___Lcom_billding_ButtonBehaviors\n   var e$1 = $m_Lcom_billding_OutterStyles$().normalButton$1.apply__O__O__O(\"Reset the scene!\", buttonBehaviors.initiateSceneReset$1);\n   var jsx$2 = new $c_Lscalatags_LowPriorityImplicits$bindNode().init___Lscalatags_LowPriorityImplicits__Lorg_scalajs_dom_raw_Node(this$7, e$1);\n   var this$8 = $m_Lscalatags_JsDom$all$();\n-  var e$3 = $m_Lcom_billding_OutterStyles$().dangerButton$1.apply__O__O__O(\"Disrupt the flow\", buttonBehaviors.toggleDisruptExisting$1);\n+  var e$3 = $m_Lcom_billding_OutterStyles$().dangerButton$1.apply__O__O__O(\"Make 1 car brake\", buttonBehaviors.toggleDisruptExisting$1);\n   var array$1 = [jsx$3, jsx$2, new $c_Lscalatags_LowPriorityImplicits$bindNode().init___Lscalatags_LowPriorityImplicits__Lorg_scalajs_dom_raw_Node(this$8, e$3)];\n   this.buttons$1 = jsx$4.apply__sc_Seq__Lscalatags_JsDom$TypedTag(new $c_sjs_js_WrappedArray().init___sjs_js_Array(array$1)).render__Lorg_scalajs_dom_raw_Element();\n   var this$11 = $m_Lscalatags_JsDom$all$();\n@@ -30962,7 +30962,7 @@ $c_Lcom_billding_svgRendering_SpatialCanvas.prototype.init___Lsquants_space_Leng\n   this.width$1 = width;\n   this.pixelHeight$1 = pixelHeight;\n   this.pixelWidth$1 = pixelWidth;\n-  var that = $imul(3, pixelHeight);\n+  var that = $imul(5, pixelHeight);\n   this.heightDistancePerPixel$1 = $as_Lsquants_space_Length(height.divide__D__Lsquants_Quantity(that));\n   var that$1 = $imul(3, pixelWidth);\n   this.widthDistancePerPixel$1 = $as_Lsquants_space_Length(width.divide__D__Lsquants_Quantity(that$1));"
    }
  ]
}
        """
      )
      parseResult.left.foreach(println)
      println(parseResult)
      Tree(
        "42024c49fe1b7269ff22b80d8e8477562a44b870",
      Commit(
        Author(
          "swoogles",
          "bill.frasure@gmail.com",
          Some(Instant.parse("2019-12-03T10:58:59Z"))
        ),
        "Better styling & sample scenes") ,
        "https://github.com/swoogles/TrafficSimulation/commit/42024c49fe1b7269ff22b80d8e8477562a44b870"
      ) shouldBe parseResult.right.get
    }
    "parse a repo" in {
      decode[Repo](
      """
        |    {
        |      "id": 226618655,
        |      "name": "swoogles/purelyfunctionalserver",
        |      "url": "https://api.github.com/repos/swoogles/purelyfunctionalserver"
        |    }
        | """.stripMargin
      ).right.get shouldBe Repo("swoogles/purelyfunctionalserver")
    }
    "parse a List[Commit]" in {

      decode[List[Commit]](
      """
        |      [
        |        {
        |          "sha": "e40a5f155bd3014543b47ba3e51995815f4d6b54",
        |          "author": {
        |            "email": "bill.frasure@gmail.com",
        |            "name": "swoogles"
        |          },
        |          "message": "Get the most minimal Github API working :D We are off to the races\nAlthough I desperately needed the template to get started, the types are actually starting to click now",
        |          "distinct": true,
        |          "url": "https://api.github.com/repos/swoogles/purelyfunctionalserver/commits/e40a5f155bd3014543b47ba3e51995815f4d6b54"
        |        }
        |      ]
        |""".stripMargin
      ).right.get shouldBe List(Commit(Author("swoogles", "bill.frasure@gmail.com"), "Get the most minimal Github API working :D We are off to the races\nAlthough I desperately needed the template to get started, the types are actually starting to click now"))
    }

    "parse a bit of recent user activity" in {
      decode[UserActivityEvent](
      """
        |  {
        |    "id": "11037476065",
        |    "type": "PushEvent",
        |    "actor": {
        |      "id": 2054940,
        |      "login": "swoogles",
        |      "display_login": "swoogles",
        |      "gravatar_id": "",
        |      "url": "https://api.github.com/users/swoogles",
        |      "avatar_url": "https://avatars.githubusercontent.com/u/2054940?"
        |    },
        |    "repo": {
        |      "id": 226618655,
        |      "name": "swoogles/purelyfunctionalserver",
        |      "url": "https://api.github.com/repos/swoogles/purelyfunctionalserver"
        |    },
        |    "payload": {
        |      "push_id": 4369278436,
        |      "size": 1,
        |      "distinct_size": 1,
        |      "ref": "refs/heads/master",
        |      "head": "e40a5f155bd3014543b47ba3e51995815f4d6b54",
        |      "before": "bc76ea6378d75dc796f9213a5d6f9e07a9ffa4c3",
        |      "commits": [
        |        {
        |          "sha": "e40a5f155bd3014543b47ba3e51995815f4d6b54",
        |          "author": {
        |            "email": "bill.frasure@gmail.com",
        |            "name": "swoogles"
        |          },
        |          "message": "Get the most minimal Github API working :D We are off to the races\nAlthough I desperately needed the template to get started, the types are actually starting to click now",
        |          "distinct": true,
        |          "url": "https://api.github.com/repos/swoogles/purelyfunctionalserver/commits/e40a5f155bd3014543b47ba3e51995815f4d6b54"
        |        }
        |      ]
        |    },
        |    "public": true,
        |    "created_at": "2019-12-08T06:19:42Z"
        |  }
        |
        |
        |""".stripMargin
      ).right.get shouldBe UserActivityEvent(Repo("swoogles/purelyfunctionalserver"), Payload(Some(List(Commit(Author("swoogles", "bill.frasure@gmail.com"), "Get the most minimal Github API working :D We are off to the races\nAlthough I desperately needed the template to get started, the types are actually starting to click now")))))
    }
    "parse a all recent user activity" in {
      decode[List[UserActivityEvent]](
        """
          |[
          |  {
          |    "id": "11037476065",
          |    "type": "PushEvent",
          |    "actor": {
          |      "id": 2054940,
          |      "login": "swoogles",
          |      "display_login": "swoogles",
          |      "gravatar_id": "",
          |      "url": "https://api.github.com/users/swoogles",
          |      "avatar_url": "https://avatars.githubusercontent.com/u/2054940?"
          |    },
          |    "repo": {
          |      "id": 226618655,
          |      "name": "swoogles/purelyfunctionalserver",
          |      "url": "https://api.github.com/repos/swoogles/purelyfunctionalserver"
          |    },
          |    "payload": {
          |      "push_id": 4369278436,
          |      "size": 1,
          |      "distinct_size": 1,
          |      "ref": "refs/heads/master",
          |      "head": "e40a5f155bd3014543b47ba3e51995815f4d6b54",
          |      "before": "bc76ea6378d75dc796f9213a5d6f9e07a9ffa4c3",
          |      "commits": [
          |        {
          |          "sha": "e40a5f155bd3014543b47ba3e51995815f4d6b54",
          |          "author": {
          |            "email": "bill.frasure@gmail.com",
          |            "name": "swoogles"
          |          },
          |          "message": "Get the most minimal Github API working :D We are off to the races\nAlthough I desperately needed the template to get started, the types are actually starting to click now",
          |          "distinct": true,
          |          "url": "https://api.github.com/repos/swoogles/purelyfunctionalserver/commits/e40a5f155bd3014543b47ba3e51995815f4d6b54"
          |        }
          |      ]
          |    },
          |    "public": true,
          |    "created_at": "2019-12-08T06:19:42Z"
          |  },
          |  {
          |    "id": "11037396350",
          |    "type": "PushEvent",
          |    "actor": {
          |      "id": 2054940,
          |      "login": "swoogles",
          |      "display_login": "swoogles",
          |      "gravatar_id": "",
          |      "url": "https://api.github.com/users/swoogles",
          |      "avatar_url": "https://avatars.githubusercontent.com/u/2054940?"
          |    },
          |    "repo": {
          |      "id": 226618655,
          |      "name": "swoogles/purelyfunctionalserver",
          |      "url": "https://api.github.com/repos/swoogles/purelyfunctionalserver"
          |    },
          |    "payload": {
          |      "push_id": 4369224570,
          |      "size": 1,
          |      "distinct_size": 1,
          |      "ref": "refs/heads/master",
          |      "head": "bc76ea6378d75dc796f9213a5d6f9e07a9ffa4c3",
          |      "before": "8a3bcd31f94c7ce23ac9e6bc91133769478b211b",
          |      "commits": [
          |        {
          |          "sha": "bc76ea6378d75dc796f9213a5d6f9e07a9ffa4c3",
          |          "author": {
          |            "email": "bill.frasure@gmail.com",
          |            "name": "swoogles"
          |          },
          |          "message": "Maaaaybe get things deployed to heroku and connected to their DB",
          |          "distinct": true,
          |          "url": "https://api.github.com/repos/swoogles/purelyfunctionalserver/commits/bc76ea6378d75dc796f9213a5d6f9e07a9ffa4c3"
          |        }
          |      ]
          |    },
          |    "public": true,
          |    "created_at": "2019-12-08T05:29:16Z"
          |  },
          |  {
          |    "id": "11037381099",
          |    "type": "CreateEvent",
          |    "actor": {
          |      "id": 2054940,
          |      "login": "swoogles",
          |      "display_login": "swoogles",
          |      "gravatar_id": "",
          |      "url": "https://api.github.com/users/swoogles",
          |      "avatar_url": "https://avatars.githubusercontent.com/u/2054940?"
          |    },
          |    "repo": {
          |      "id": 226618655,
          |      "name": "swoogles/purelyfunctionalserver",
          |      "url": "https://api.github.com/repos/swoogles/purelyfunctionalserver"
          |    },
          |    "payload": {
          |      "ref": "master",
          |      "ref_type": "branch",
          |      "master_branch": "master",
          |      "description": null,
          |      "pusher_type": "user"
          |    },
          |    "public": true,
          |    "created_at": "2019-12-08T05:19:49Z"
          |  },
          |  {
          |    "id": "11037380016",
          |    "type": "CreateEvent",
          |    "actor": {
          |      "id": 2054940,
          |      "login": "swoogles",
          |      "display_login": "swoogles",
          |      "gravatar_id": "",
          |      "url": "https://api.github.com/users/swoogles",
          |      "avatar_url": "https://avatars.githubusercontent.com/u/2054940?"
          |    },
          |    "repo": {
          |      "id": 226618655,
          |      "name": "swoogles/purelyfunctionalserver",
          |      "url": "https://api.github.com/repos/swoogles/purelyfunctionalserver"
          |    },
          |    "payload": {
          |      "ref": null,
          |      "ref_type": "repository",
          |      "master_branch": "master",
          |      "description": null,
          |      "pusher_type": "user"
          |    },
          |    "public": true,
          |    "created_at": "2019-12-08T05:19:12Z"
          |  },
          |  {
          |    "id": "11025684743",
          |    "type": "PushEvent",
          |    "actor": {
          |      "id": 2054940,
          |      "login": "swoogles",
          |      "display_login": "swoogles",
          |      "gravatar_id": "",
          |      "url": "https://api.github.com/users/swoogles",
          |      "avatar_url": "https://avatars.githubusercontent.com/u/2054940?"
          |    },
          |    "repo": {
          |      "id": 226214131,
          |      "name": "swoogles/purelyfunctionalserver",
          |      "url": "https://api.github.com/repos/swoogles/purelyfunctionalserver"
          |    },
          |    "payload": {
          |      "push_id": 4362660433,
          |      "size": 1,
          |      "distinct_size": 1,
          |      "ref": "refs/heads/master",
          |      "head": "94f358878cf94f13afc6cfbe9bd70dd94f07143c",
          |      "before": "6a9c51365e8c0abda4f9bab49fc75da027e004a2",
          |      "commits": [
          |        {
          |          "sha": "94f358878cf94f13afc6cfbe9bd70dd94f07143c",
          |          "author": {
          |            "email": "bill.frasure@gmail.com",
          |            "name": "swoogles"
          |          },
          |          "message": "Trying to turn IO into F[_] to get things compiling. Really flailing here.",
          |          "distinct": true,
          |          "url": "https://api.github.com/repos/swoogles/purelyfunctionalserver/commits/94f358878cf94f13afc6cfbe9bd70dd94f07143c"
          |        }
          |      ]
          |    },
          |    "public": true,
          |    "created_at": "2019-12-06T03:36:09Z"
          |  },
          |  {
          |    "id": "11025535277",
          |    "type": "PushEvent",
          |    "actor": {
          |      "id": 2054940,
          |      "login": "swoogles",
          |      "display_login": "swoogles",
          |      "gravatar_id": "",
          |      "url": "https://api.github.com/users/swoogles",
          |      "avatar_url": "https://avatars.githubusercontent.com/u/2054940?"
          |    },
          |    "repo": {
          |      "id": 226214131,
          |      "name": "swoogles/purelyfunctionalserver",
          |      "url": "https://api.github.com/repos/swoogles/purelyfunctionalserver"
          |    },
          |    "payload": {
          |      "push_id": 4362575543,
          |      "size": 1,
          |      "distinct_size": 1,
          |      "ref": "refs/heads/master",
          |      "head": "6a9c51365e8c0abda4f9bab49fc75da027e004a2",
          |      "before": "29b4de03b1fc9cf72b09759aff9d900f123bb3a4",
          |      "commits": [
          |        {
          |          "sha": "6a9c51365e8c0abda4f9bab49fc75da027e004a2",
          |          "author": {
          |            "email": "bill.frasure@gmail.com",
          |            "name": "swoogles"
          |          },
          |          "message": "All kinds of flailing towards Doobie",
          |          "distinct": true,
          |          "url": "https://api.github.com/repos/swoogles/purelyfunctionalserver/commits/6a9c51365e8c0abda4f9bab49fc75da027e004a2"
          |        }
          |      ]
          |    },
          |    "public": true,
          |    "created_at": "2019-12-06T03:01:05Z"
          |  },
          |  {
          |    "id": "11025168224",
          |    "type": "PushEvent",
          |    "actor": {
          |      "id": 2054940,
          |      "login": "swoogles",
          |      "display_login": "swoogles",
          |      "gravatar_id": "",
          |      "url": "https://api.github.com/users/swoogles",
          |      "avatar_url": "https://avatars.githubusercontent.com/u/2054940?"
          |    },
          |    "repo": {
          |      "id": 226214131,
          |      "name": "swoogles/purelyfunctionalserver",
          |      "url": "https://api.github.com/repos/swoogles/purelyfunctionalserver"
          |    },
          |    "payload": {
          |      "push_id": 4362373413,
          |      "size": 1,
          |      "distinct_size": 1,
          |      "ref": "refs/heads/master",
          |      "head": "29b4de03b1fc9cf72b09759aff9d900f123bb3a4",
          |      "before": "c2603249c671853d85251f65f6438b81bc0b77e6",
          |      "commits": [
          |        {
          |          "sha": "29b4de03b1fc9cf72b09759aff9d900f123bb3a4",
          |          "author": {
          |            "email": "bill.frasure@gmail.com",
          |            "name": "swoogles"
          |          },
          |          "message": "Consult PORT environment variable during Blaze startup",
          |          "distinct": true,
          |          "url": "https://api.github.com/repos/swoogles/purelyfunctionalserver/commits/29b4de03b1fc9cf72b09759aff9d900f123bb3a4"
          |        }
          |      ]
          |    },
          |    "public": true,
          |    "created_at": "2019-12-06T01:37:45Z"
          |  },
          |  {
          |    "id": "11024904361",
          |    "type": "PushEvent",
          |    "actor": {
          |      "id": 2054940,
          |      "login": "swoogles",
          |      "display_login": "swoogles",
          |      "gravatar_id": "",
          |      "url": "https://api.github.com/users/swoogles",
          |      "avatar_url": "https://avatars.githubusercontent.com/u/2054940?"
          |    },
          |    "repo": {
          |      "id": 226214131,
          |      "name": "swoogles/purelyfunctionalserver",
          |      "url": "https://api.github.com/repos/swoogles/purelyfunctionalserver"
          |    },
          |    "payload": {
          |      "push_id": 4362229120,
          |      "size": 1,
          |      "distinct_size": 1,
          |      "ref": "refs/heads/master",
          |      "head": "c2603249c671853d85251f65f6438b81bc0b77e6",
          |      "before": "ef8b8b12210d0323ebc2a73ec870e0e73afe06b7",
          |      "commits": [
          |        {
          |          "sha": "c2603249c671853d85251f65f6438b81bc0b77e6",
          |          "author": {
          |            "email": "bill.frasure@gmail.com",
          |            "name": "swoogles"
          |          },
          |          "message": "Commit build.properties files. whoops",
          |          "distinct": true,
          |          "url": "https://api.github.com/repos/swoogles/purelyfunctionalserver/commits/c2603249c671853d85251f65f6438b81bc0b77e6"
          |        }
          |      ]
          |    },
          |    "public": true,
          |    "created_at": "2019-12-06T00:38:00Z"
          |  },
          |  {
          |    "id": "11024896073",
          |    "type": "PushEvent",
          |    "actor": {
          |      "id": 2054940,
          |      "login": "swoogles",
          |      "display_login": "swoogles",
          |      "gravatar_id": "",
          |      "url": "https://api.github.com/users/swoogles",
          |      "avatar_url": "https://avatars.githubusercontent.com/u/2054940?"
          |    },
          |    "repo": {
          |      "id": 226214131,
          |      "name": "swoogles/purelyfunctionalserver",
          |      "url": "https://api.github.com/repos/swoogles/purelyfunctionalserver"
          |    },
          |    "payload": {
          |      "push_id": 4362224580,
          |      "size": 1,
          |      "distinct_size": 1,
          |      "ref": "refs/heads/master",
          |      "head": "ef8b8b12210d0323ebc2a73ec870e0e73afe06b7",
          |      "before": "de34449ac3230fe2035248ce102b6479df0a84d6",
          |      "commits": [
          |        {
          |          "sha": "ef8b8b12210d0323ebc2a73ec870e0e73afe06b7",
          |          "author": {
          |            "email": "bill.frasure@gmail.com",
          |            "name": "swoogles"
          |          },
          |          "message": "Add Procfile",
          |          "distinct": true,
          |          "url": "https://api.github.com/repos/swoogles/purelyfunctionalserver/commits/ef8b8b12210d0323ebc2a73ec870e0e73afe06b7"
          |        }
          |      ]
          |    },
          |    "public": true,
          |    "created_at": "2019-12-06T00:36:10Z"
          |  },
          |  {
          |    "id": "11024884979",
          |    "type": "CreateEvent",
          |    "actor": {
          |      "id": 2054940,
          |      "login": "swoogles",
          |      "display_login": "swoogles",
          |      "gravatar_id": "",
          |      "url": "https://api.github.com/users/swoogles",
          |      "avatar_url": "https://avatars.githubusercontent.com/u/2054940?"
          |    },
          |    "repo": {
          |      "id": 226214131,
          |      "name": "swoogles/purelyfunctionalserver",
          |      "url": "https://api.github.com/repos/swoogles/purelyfunctionalserver"
          |    },
          |    "payload": {
          |      "ref": "master",
          |      "ref_type": "branch",
          |      "master_branch": "master",
          |      "description": "I want to really exercise ZIO/Cats here.",
          |      "pusher_type": "user"
          |    },
          |    "public": true,
          |    "created_at": "2019-12-06T00:33:38Z"
          |  },
          |  {
          |    "id": "11024883650",
          |    "type": "CreateEvent",
          |    "actor": {
          |      "id": 2054940,
          |      "login": "swoogles",
          |      "display_login": "swoogles",
          |      "gravatar_id": "",
          |      "url": "https://api.github.com/users/swoogles",
          |      "avatar_url": "https://avatars.githubusercontent.com/u/2054940?"
          |    },
          |    "repo": {
          |      "id": 226214131,
          |      "name": "swoogles/purelyfunctionalserver",
          |      "url": "https://api.github.com/repos/swoogles/purelyfunctionalserver"
          |    },
          |    "payload": {
          |      "ref": null,
          |      "ref_type": "repository",
          |      "master_branch": "master",
          |      "description": "I want to really exercise ZIO/Cats here.",
          |      "pusher_type": "user"
          |    },
          |    "public": true,
          |    "created_at": "2019-12-06T00:33:21Z"
          |  },
          |  {
          |    "id": "11001786519",
          |    "type": "WatchEvent",
          |    "actor": {
          |      "id": 2054940,
          |      "login": "swoogles",
          |      "display_login": "swoogles",
          |      "gravatar_id": "",
          |      "url": "https://api.github.com/users/swoogles",
          |      "avatar_url": "https://avatars.githubusercontent.com/u/2054940?"
          |    },
          |    "repo": {
          |      "id": 173121216,
          |      "name": "zio/zio-kafka",
          |      "url": "https://api.github.com/repos/zio/zio-kafka"
          |    },
          |    "payload": {
          |      "action": "started"
          |    },
          |    "public": true,
          |    "created_at": "2019-12-03T17:07:48Z",
          |    "org": {
          |      "id": 49655448,
          |      "login": "zio",
          |      "gravatar_id": "",
          |      "url": "https://api.github.com/orgs/zio",
          |      "avatar_url": "https://avatars.githubusercontent.com/u/49655448?"
          |    }
          |  },
          |  {
          |    "id": "10995352510",
          |    "type": "PushEvent",
          |    "actor": {
          |      "id": 2054940,
          |      "login": "swoogles",
          |      "display_login": "swoogles",
          |      "gravatar_id": "",
          |      "url": "https://api.github.com/users/swoogles",
          |      "avatar_url": "https://avatars.githubusercontent.com/u/2054940?"
          |    },
          |    "repo": {
          |      "id": 89831403,
          |      "name": "swoogles/TrafficSimulation",
          |      "url": "https://api.github.com/repos/swoogles/TrafficSimulation"
          |    },
          |    "payload": {
          |      "push_id": 4346787201,
          |      "size": 1,
          |      "distinct_size": 1,
          |      "ref": "refs/heads/master",
          |      "head": "42024c49fe1b7269ff22b80d8e8477562a44b870",
          |      "before": "b7867927838450bfe1c9d5bfb0365fe3e2bfbeaf",
          |      "commits": [
          |        {
          |          "sha": "42024c49fe1b7269ff22b80d8e8477562a44b870",
          |          "author": {
          |            "email": "bill.frasure@gmail.com",
          |            "name": "swoogles"
          |          },
          |          "message": "Better styling & sample scenes",
          |          "distinct": true,
          |          "url": "https://api.github.com/repos/swoogles/TrafficSimulation/commits/42024c49fe1b7269ff22b80d8e8477562a44b870"
          |        }
          |      ]
          |    },
          |    "public": true,
          |    "created_at": "2019-12-03T02:53:14Z"
          |  },
          |  {
          |    "id": "10995215534",
          |    "type": "PushEvent",
          |    "actor": {
          |      "id": 2054940,
          |      "login": "swoogles",
          |      "display_login": "swoogles",
          |      "gravatar_id": "",
          |      "url": "https://api.github.com/users/swoogles",
          |      "avatar_url": "https://avatars.githubusercontent.com/u/2054940?"
          |    },
          |    "repo": {
          |      "id": 89831403,
          |      "name": "swoogles/TrafficSimulation",
          |      "url": "https://api.github.com/repos/swoogles/TrafficSimulation"
          |    },
          |    "payload": {
          |      "push_id": 4346715860,
          |      "size": 3,
          |      "distinct_size": 3,
          |      "ref": "refs/heads/master",
          |      "head": "b7867927838450bfe1c9d5bfb0365fe3e2bfbeaf",
          |      "before": "94d8ea8143ac6b9b4b14bbf9425e9d7ebebc49e6",
          |      "commits": [
          |        {
          |          "sha": "7333e92aa8437ef8c996e848328c93f588b122a1",
          |          "author": {
          |            "email": "bill.frasure@gmail.com",
          |            "name": "swoogles"
          |          },
          |          "message": " #time 20m Fix DT control.",
          |          "distinct": true,
          |          "url": "https://api.github.com/repos/swoogles/TrafficSimulation/commits/7333e92aa8437ef8c996e848328c93f588b122a1"
          |        },
          |        {
          |          "sha": "962b73ca53df3e58143883c3e847283ab6e083e7",
          |          "author": {
          |            "email": "bill.frasure@gmail.com",
          |            "name": "swoogles"
          |          },
          |          "message": "Work on getting the cars to be a reasonable side. Still very confusing.",
          |          "distinct": true,
          |          "url": "https://api.github.com/repos/swoogles/TrafficSimulation/commits/962b73ca53df3e58143883c3e847283ab6e083e7"
          |        },
          |        {
          |          "sha": "b7867927838450bfe1c9d5bfb0365fe3e2bfbeaf",
          |          "author": {
          |            "email": "bill.frasure@gmail.com",
          |            "name": "swoogles"
          |          },
          |          "message": "Start fixing Sample scenes to be more informative",
          |          "distinct": true,
          |          "url": "https://api.github.com/repos/swoogles/TrafficSimulation/commits/b7867927838450bfe1c9d5bfb0365fe3e2bfbeaf"
          |        }
          |      ]
          |    },
          |    "public": true,
          |    "created_at": "2019-12-03T02:25:11Z"
          |  },
          |  {
          |    "id": "10984985749",
          |    "type": "WatchEvent",
          |    "actor": {
          |      "id": 2054940,
          |      "login": "swoogles",
          |      "display_login": "swoogles",
          |      "gravatar_id": "",
          |      "url": "https://api.github.com/users/swoogles",
          |      "avatar_url": "https://avatars.githubusercontent.com/u/2054940?"
          |    },
          |    "repo": {
          |      "id": 192378390,
          |      "name": "Clover-Group/zio-template.g8",
          |      "url": "https://api.github.com/repos/Clover-Group/zio-template.g8"
          |    },
          |    "payload": {
          |      "action": "started"
          |    },
          |    "public": true,
          |    "created_at": "2019-12-02T00:24:11Z",
          |    "org": {
          |      "id": 42359892,
          |      "login": "Clover-Group",
          |      "gravatar_id": "",
          |      "url": "https://api.github.com/orgs/Clover-Group",
          |      "avatar_url": "https://avatars.githubusercontent.com/u/42359892?"
          |    }
          |  },
          |  {
          |    "id": "10984922627",
          |    "type": "PushEvent",
          |    "actor": {
          |      "id": 2054940,
          |      "login": "swoogles",
          |      "display_login": "swoogles",
          |      "gravatar_id": "",
          |      "url": "https://api.github.com/users/swoogles",
          |      "avatar_url": "https://avatars.githubusercontent.com/u/2054940?"
          |    },
          |    "repo": {
          |      "id": 179774781,
          |      "name": "swoogles/bionicBodyFitness",
          |      "url": "https://api.github.com/repos/swoogles/bionicBodyFitness"
          |    },
          |    "payload": {
          |      "push_id": 4341402852,
          |      "size": 1,
          |      "distinct_size": 1,
          |      "ref": "refs/heads/master",
          |      "head": "4815d73e9f568d9f79d309c1fc50760f329ed588",
          |      "before": "c71bae93d2026530375d69d3246236495a324206",
          |      "commits": [
          |        {
          |          "sha": "4815d73e9f568d9f79d309c1fc50760f329ed588",
          |          "author": {
          |            "email": "bill.frasure@gmail.com",
          |            "name": "swoogles"
          |          },
          |          "message": "Update credits to BilldingSoftware",
          |          "distinct": true,
          |          "url": "https://api.github.com/repos/swoogles/bionicBodyFitness/commits/4815d73e9f568d9f79d309c1fc50760f329ed588"
          |        }
          |      ]
          |    },
          |    "public": true,
          |    "created_at": "2019-12-02T00:01:28Z"
          |  },
          |  {
          |    "id": "10984639791",
          |    "type": "PushEvent",
          |    "actor": {
          |      "id": 2054940,
          |      "login": "swoogles",
          |      "display_login": "swoogles",
          |      "gravatar_id": "",
          |      "url": "https://api.github.com/users/swoogles",
          |      "avatar_url": "https://avatars.githubusercontent.com/u/2054940?"
          |    },
          |    "repo": {
          |      "id": 224943367,
          |      "name": "swoogles/CbTrailMapsDataConversion",
          |      "url": "https://api.github.com/repos/swoogles/CbTrailMapsDataConversion"
          |    },
          |    "payload": {
          |      "push_id": 4341224174,
          |      "size": 1,
          |      "distinct_size": 1,
          |      "ref": "refs/heads/master",
          |      "head": "7c6cb6c843c1dabe8cc77ae4b17b493590db0cdc",
          |      "before": "2c93676ee533bfc80b6560f7e9d93d02287538b6",
          |      "commits": [
          |        {
          |          "sha": "7c6cb6c843c1dabe8cc77ae4b17b493590db0cdc",
          |          "author": {
          |            "email": "bill.frasure@gmail.com",
          |            "name": "swoogles"
          |          },
          |          "message": "#10m Give output files correct GPX extension",
          |          "distinct": true,
          |          "url": "https://api.github.com/repos/swoogles/CbTrailMapsDataConversion/commits/7c6cb6c843c1dabe8cc77ae4b17b493590db0cdc"
          |        }
          |      ]
          |    },
          |    "public": true,
          |    "created_at": "2019-12-01T22:13:47Z"
          |  },
          |  {
          |    "id": "10981709449",
          |    "type": "WatchEvent",
          |    "actor": {
          |      "id": 2054940,
          |      "login": "swoogles",
          |      "display_login": "swoogles",
          |      "gravatar_id": "",
          |      "url": "https://api.github.com/users/swoogles",
          |      "avatar_url": "https://avatars.githubusercontent.com/u/2054940?"
          |    },
          |    "repo": {
          |      "id": 76488267,
          |      "name": "jenetics/jpx",
          |      "url": "https://api.github.com/repos/jenetics/jpx"
          |    },
          |    "payload": {
          |      "action": "started"
          |    },
          |    "public": true,
          |    "created_at": "2019-12-01T00:19:34Z"
          |  },
          |  {
          |    "id": "10981526005",
          |    "type": "PushEvent",
          |    "actor": {
          |      "id": 2054940,
          |      "login": "swoogles",
          |      "display_login": "swoogles",
          |      "gravatar_id": "",
          |      "url": "https://api.github.com/users/swoogles",
          |      "avatar_url": "https://avatars.githubusercontent.com/u/2054940?"
          |    },
          |    "repo": {
          |      "id": 224943367,
          |      "name": "swoogles/CbTrailMapsDataConversion",
          |      "url": "https://api.github.com/repos/swoogles/CbTrailMapsDataConversion"
          |    },
          |    "payload": {
          |      "push_id": 4339234626,
          |      "size": 2,
          |      "distinct_size": 2,
          |      "ref": "refs/heads/master",
          |      "head": "2c93676ee533bfc80b6560f7e9d93d02287538b6",
          |      "before": "e3f5f067d8f83f33185d90adb7caa291986eea8a",
          |      "commits": [
          |        {
          |          "sha": "ffb4129fb43d870a67bfc00e10bee2f926d43c16",
          |          "author": {
          |            "email": "bill.frasure@gmail.com",
          |            "name": "swoogles"
          |          },
          |          "message": "NOT-BILLABLE #time 15m #comment Rip out extra crud",
          |          "distinct": true,
          |          "url": "https://api.github.com/repos/swoogles/CbTrailMapsDataConversion/commits/ffb4129fb43d870a67bfc00e10bee2f926d43c16"
          |        },
          |        {
          |          "sha": "2c93676ee533bfc80b6560f7e9d93d02287538b6",
          |          "author": {
          |            "email": "bill.frasure@gmail.com",
          |            "name": "swoogles"
          |          },
          |          "message": "NOT-BILLABLE #time 10m #comment Better ZIO structuring",
          |          "distinct": true,
          |          "url": "https://api.github.com/repos/swoogles/CbTrailMapsDataConversion/commits/2c93676ee533bfc80b6560f7e9d93d02287538b6"
          |        }
          |      ]
          |    },
          |    "public": true,
          |    "created_at": "2019-11-30T22:36:11Z"
          |  },
          |  {
          |    "id": "10979055973",
          |    "type": "CreateEvent",
          |    "actor": {
          |      "id": 2054940,
          |      "login": "swoogles",
          |      "display_login": "swoogles",
          |      "gravatar_id": "",
          |      "url": "https://api.github.com/users/swoogles",
          |      "avatar_url": "https://avatars.githubusercontent.com/u/2054940?"
          |    },
          |    "repo": {
          |      "id": 224943367,
          |      "name": "swoogles/CbTrailMapsDataConversion",
          |      "url": "https://api.github.com/repos/swoogles/CbTrailMapsDataConversion"
          |    },
          |    "payload": {
          |      "ref": "master",
          |      "ref_type": "branch",
          |      "master_branch": "master",
          |      "description": "One-off project to help Daniel's dog get his trail completion record",
          |      "pusher_type": "user"
          |    },
          |    "public": true,
          |    "created_at": "2019-11-30T01:25:09Z"
          |  },
          |  {
          |    "id": "10979055429",
          |    "type": "CreateEvent",
          |    "actor": {
          |      "id": 2054940,
          |      "login": "swoogles",
          |      "display_login": "swoogles",
          |      "gravatar_id": "",
          |      "url": "https://api.github.com/users/swoogles",
          |      "avatar_url": "https://avatars.githubusercontent.com/u/2054940?"
          |    },
          |    "repo": {
          |      "id": 224943367,
          |      "name": "swoogles/CbTrailMapsDataConversion",
          |      "url": "https://api.github.com/repos/swoogles/CbTrailMapsDataConversion"
          |    },
          |    "payload": {
          |      "ref": null,
          |      "ref_type": "repository",
          |      "master_branch": "master",
          |      "description": "One-off project to help Daniel's dog get his trail completion record",
          |      "pusher_type": "user"
          |    },
          |    "public": true,
          |    "created_at": "2019-11-30T01:24:51Z"
          |  },
          |  {
          |    "id": "10962583454",
          |    "type": "WatchEvent",
          |    "actor": {
          |      "id": 2054940,
          |      "login": "swoogles",
          |      "display_login": "swoogles",
          |      "gravatar_id": "",
          |      "url": "https://api.github.com/users/swoogles",
          |      "avatar_url": "https://avatars.githubusercontent.com/u/2054940?"
          |    },
          |    "repo": {
          |      "id": 48071068,
          |      "name": "zamblauskas/scala-csv-parser",
          |      "url": "https://api.github.com/repos/zamblauskas/scala-csv-parser"
          |    },
          |    "payload": {
          |      "action": "started"
          |    },
          |    "public": true,
          |    "created_at": "2019-11-27T17:34:25Z"
          |  },
          |  {
          |    "id": "10926345025",
          |    "type": "WatchEvent",
          |    "actor": {
          |      "id": 2054940,
          |      "login": "swoogles",
          |      "display_login": "swoogles",
          |      "gravatar_id": "",
          |      "url": "https://api.github.com/users/swoogles",
          |      "avatar_url": "https://avatars.githubusercontent.com/u/2054940?"
          |    },
          |    "repo": {
          |      "id": 82961926,
          |      "name": "oyvindberg/ScalablyTyped",
          |      "url": "https://api.github.com/repos/oyvindberg/ScalablyTyped"
          |    },
          |    "payload": {
          |      "action": "started"
          |    },
          |    "public": true,
          |    "created_at": "2019-11-22T14:32:22Z"
          |  },
          |  {
          |    "id": "10899048714",
          |    "type": "WatchEvent",
          |    "actor": {
          |      "id": 2054940,
          |      "login": "swoogles",
          |      "display_login": "swoogles",
          |      "gravatar_id": "",
          |      "url": "https://api.github.com/users/swoogles",
          |      "avatar_url": "https://avatars.githubusercontent.com/u/2054940?"
          |    },
          |    "repo": {
          |      "id": 165534369,
          |      "name": "tindzk/seed",
          |      "url": "https://api.github.com/repos/tindzk/seed"
          |    },
          |    "payload": {
          |      "action": "started"
          |    },
          |    "public": true,
          |    "created_at": "2019-11-19T17:24:18Z"
          |  },
          |  {
          |    "id": "10881923067",
          |    "type": "PushEvent",
          |    "actor": {
          |      "id": 2054940,
          |      "login": "swoogles",
          |      "display_login": "swoogles",
          |      "gravatar_id": "",
          |      "url": "https://api.github.com/users/swoogles",
          |      "avatar_url": "https://avatars.githubusercontent.com/u/2054940?"
          |    },
          |    "repo": {
          |      "id": 222054189,
          |      "name": "swoogles/InteractiveLogo",
          |      "url": "https://api.github.com/repos/swoogles/InteractiveLogo"
          |    },
          |    "payload": {
          |      "push_id": 4285107926,
          |      "size": 1,
          |      "distinct_size": 1,
          |      "ref": "refs/heads/master",
          |      "head": "d2adf7b75576b594ae630b1d875ab9c174942757",
          |      "before": "0996cdae57297218299c34b4a1b6c2ebc2bb1732",
          |      "commits": [
          |        {
          |          "sha": "d2adf7b75576b594ae630b1d875ab9c174942757",
          |          "author": {
          |            "email": "bill.frasure@gmail.com",
          |            "name": "swoogles"
          |          },
          |          "message": "Good mixture of appearing and sliding logo pieces :)",
          |          "distinct": true,
          |          "url": "https://api.github.com/repos/swoogles/InteractiveLogo/commits/d2adf7b75576b594ae630b1d875ab9c174942757"
          |        }
          |      ]
          |    },
          |    "public": true,
          |    "created_at": "2019-11-17T20:50:06Z"
          |  },
          |  {
          |    "id": "10879276183",
          |    "type": "PushEvent",
          |    "actor": {
          |      "id": 2054940,
          |      "login": "swoogles",
          |      "display_login": "swoogles",
          |      "gravatar_id": "",
          |      "url": "https://api.github.com/users/swoogles",
          |      "avatar_url": "https://avatars.githubusercontent.com/u/2054940?"
          |    },
          |    "repo": {
          |      "id": 222054189,
          |      "name": "swoogles/InteractiveLogo",
          |      "url": "https://api.github.com/repos/swoogles/InteractiveLogo"
          |    },
          |    "payload": {
          |      "push_id": 4283396979,
          |      "size": 4,
          |      "distinct_size": 4,
          |      "ref": "refs/heads/master",
          |      "head": "0996cdae57297218299c34b4a1b6c2ebc2bb1732",
          |      "before": "2146c1ad48e87e0d70008801f78be43920167508",
          |      "commits": [
          |        {
          |          "sha": "13e92ffc80caebf090b2706ccf92fab06643f7df",
          |          "author": {
          |            "email": "bill.frasure@gmail.com",
          |            "name": "swoogles"
          |          },
          |          "message": "Create function for targetting specific sections",
          |          "distinct": true,
          |          "url": "https://api.github.com/repos/swoogles/InteractiveLogo/commits/13e92ffc80caebf090b2706ccf92fab06643f7df"
          |        },
          |        {
          |          "sha": "29690bb5f6579bb57414babb727288d1f53f3b6d",
          |          "author": {
          |            "email": "bill.frasure@gmail.com",
          |            "name": "swoogles"
          |          },
          |          "message": "Reveal light side elements 1 per second",
          |          "distinct": true,
          |          "url": "https://api.github.com/repos/swoogles/InteractiveLogo/commits/29690bb5f6579bb57414babb727288d1f53f3b6d"
          |        },
          |        {
          |          "sha": "09c0ff1b24c928b7270cce0192054fbe6860b566",
          |          "author": {
          |            "email": "bill.frasure@gmail.com",
          |            "name": "swoogles"
          |          },
          |          "message": "Reveal everything but the counterspaces :D",
          |          "distinct": true,
          |          "url": "https://api.github.com/repos/swoogles/InteractiveLogo/commits/09c0ff1b24c928b7270cce0192054fbe6860b566"
          |        },
          |        {
          |          "sha": "0996cdae57297218299c34b4a1b6c2ebc2bb1732",
          |          "author": {
          |            "email": "bill.frasure@gmail.com",
          |            "name": "swoogles"
          |          },
          |          "message": "First tinkering with moving the svg sections",
          |          "distinct": true,
          |          "url": "https://api.github.com/repos/swoogles/InteractiveLogo/commits/0996cdae57297218299c34b4a1b6c2ebc2bb1732"
          |        }
          |      ]
          |    },
          |    "public": true,
          |    "created_at": "2019-11-17T00:06:04Z"
          |  },
          |  {
          |    "id": "10876963006",
          |    "type": "CreateEvent",
          |    "actor": {
          |      "id": 2054940,
          |      "login": "swoogles",
          |      "display_login": "swoogles",
          |      "gravatar_id": "",
          |      "url": "https://api.github.com/users/swoogles",
          |      "avatar_url": "https://avatars.githubusercontent.com/u/2054940?"
          |    },
          |    "repo": {
          |      "id": 222054189,
          |      "name": "swoogles/InteractiveLogo",
          |      "url": "https://api.github.com/repos/swoogles/InteractiveLogo"
          |    },
          |    "payload": {
          |      "ref": "master",
          |      "ref_type": "branch",
          |      "master_branch": "master",
          |      "description": "Exploring what's possible with my logo",
          |      "pusher_type": "user"
          |    },
          |    "public": true,
          |    "created_at": "2019-11-16T05:55:30Z"
          |  },
          |  {
          |    "id": "10876962534",
          |    "type": "CreateEvent",
          |    "actor": {
          |      "id": 2054940,
          |      "login": "swoogles",
          |      "display_login": "swoogles",
          |      "gravatar_id": "",
          |      "url": "https://api.github.com/users/swoogles",
          |      "avatar_url": "https://avatars.githubusercontent.com/u/2054940?"
          |    },
          |    "repo": {
          |      "id": 222054189,
          |      "name": "swoogles/InteractiveLogo",
          |      "url": "https://api.github.com/repos/swoogles/InteractiveLogo"
          |    },
          |    "payload": {
          |      "ref": null,
          |      "ref_type": "repository",
          |      "master_branch": "master",
          |      "description": "Exploring what's possible with my logo",
          |      "pusher_type": "user"
          |    },
          |    "public": true,
          |    "created_at": "2019-11-16T05:55:15Z"
          |  },
          |  {
          |    "id": "10834879419",
          |    "type": "WatchEvent",
          |    "actor": {
          |      "id": 2054940,
          |      "login": "swoogles",
          |      "display_login": "swoogles",
          |      "gravatar_id": "",
          |      "url": "https://api.github.com/users/swoogles",
          |      "avatar_url": "https://avatars.githubusercontent.com/u/2054940?"
          |    },
          |    "repo": {
          |      "id": 20490280,
          |      "name": "krasserm/streamz",
          |      "url": "https://api.github.com/repos/krasserm/streamz"
          |    },
          |    "payload": {
          |      "action": "started"
          |    },
          |    "public": true,
          |    "created_at": "2019-11-11T15:52:51Z"
          |  },
          |  {
          |    "id": "10829926130",
          |    "type": "PushEvent",
          |    "actor": {
          |      "id": 2054940,
          |      "login": "swoogles",
          |      "display_login": "swoogles",
          |      "gravatar_id": "",
          |      "url": "https://api.github.com/users/swoogles",
          |      "avatar_url": "https://avatars.githubusercontent.com/u/2054940?"
          |    },
          |    "repo": {
          |      "id": 89831403,
          |      "name": "swoogles/TrafficSimulation",
          |      "url": "https://api.github.com/repos/swoogles/TrafficSimulation"
          |    },
          |    "payload": {
          |      "push_id": 4257153525,
          |      "size": 1,
          |      "distinct_size": 1,
          |      "ref": "refs/heads/master",
          |      "head": "94d8ea8143ac6b9b4b14bbf9425e9d7ebebc49e6",
          |      "before": "727cb84ba70d93a69feb92ae7925778410ae2a8f",
          |      "commits": [
          |        {
          |          "sha": "94d8ea8143ac6b9b4b14bbf9425e9d7ebebc49e6",
          |          "author": {
          |            "email": "bill.frasure@gmail.com",
          |            "name": "swoogles"
          |          },
          |          "message": "Increase DT. Simple way to improve performance.",
          |          "distinct": true,
          |          "url": "https://api.github.com/repos/swoogles/TrafficSimulation/commits/94d8ea8143ac6b9b4b14bbf9425e9d7ebebc49e6"
          |        }
          |      ]
          |    },
          |    "public": true,
          |    "created_at": "2019-11-11T02:18:29Z"
          |  }
          |]
          |""".stripMargin
      ) match {
        case Left(failure) => fail(failure.getMessage)
        case Right(successfulParse) => println("passing with no real assertions")
      }
    }
  }

}
