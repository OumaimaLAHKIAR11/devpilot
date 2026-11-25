package ma.ensias.devpilot.knowledge;

import ma.ensias.devpilot.knowledge.api.KnowledgeSearch;
import ma.ensias.devpilot.knowledge.internal.FakeKnowledgeSearch;

class FakeKnowledgeSearchTest extends KnowledgeSearchContract {

    @Override
    KnowledgeSearch subject() {
        return new FakeKnowledgeSearch();
    }
}