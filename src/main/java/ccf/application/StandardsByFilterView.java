package ccf.application;

import akka.javasdk.annotations.ComponentId;
import akka.javasdk.annotations.Consume;
import akka.javasdk.annotations.Query;
import akka.javasdk.view.TableUpdater;
import akka.javasdk.view.View;
import ccf.domain.standard.StandardEvent;
import ccf.domain.standard.StandardRow;
import ccf.domain.standard.StandardRows;

@ComponentId("standards_by_filter")
public class StandardsByFilterView extends View {

    @Consume.FromEventSourcedEntity(StandardEntity.class)
    public static class StandardsByFilter extends TableUpdater<StandardRow> { // <2>
        public Effect<StandardRow> onEvent(StandardEvent event) { // <3>
            var ret = switch (event) {
                case StandardEvent.StandardCreated created->
                    effects().updateRow(new StandardRow(created.standardCreate().name(),
                            created.standardCreate().description()));
                default -> effects().ignore(); // Add default case to handle other events
            };
            return ret;
        }
    }

    @Query("SELECT * AS standards FROM standards_by_filter")
    public QueryEffect<StandardRows> getAllStandards() {
        return queryResult();
    }
}
