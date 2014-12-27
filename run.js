try {
    require("source-map-support").install();
} catch(err) {
}
require("./out/goog/bootstrap/nodejs.js");
require("./out/hello_world.js");
goog.require("hello_world.core");
goog.require("cljs.nodejscli");
