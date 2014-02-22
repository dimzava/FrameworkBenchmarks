package ratpack.benchmarks.techempower.groovy

import ratpack.benchmarks.techempower.common.World
import ratpack.handling.Context
import ratpack.handling.Handler

import static ratpack.jackson.Jackson.json

class QueryCountAcceptingBackgroundHandler implements Handler {

  Closure<World[]> inBackground

  QueryCountAcceptingBackgroundHandler(Closure<World[]> inBackground) {
    this.inBackground = inBackground
  }

  private int queryCount(String queriesParam) {
    int count = 1;
    try {
      count = Integer.parseInt(queriesParam);
      if (count < 1) {
        count = 1;
      }
      if (count > 500) {
        count = 500;
      }
    } catch (NumberFormatException e) {
      // ignore
    }
    return count;
  }

  @Override
  void handle(Context context) throws Exception {
    def worldService = context.get(WorldService)
    context.background {
      inBackground(worldService, queryCount(context.request.queryParams.queries))
    } then { World[] worlds->
      context.render(json(worlds))
    }
  }
}
