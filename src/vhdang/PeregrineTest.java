/**
 * 
 */
package vhdang;

/**
 * @author haidangvo
 *
 */

import java.io.Serializable;
import java.util.List;
import java.util.logging.Level;
import java.util.logging.Logger;

import org.erasmusmc.data_mining.ontology.api.Concept;
import org.erasmusmc.data_mining.ontology.api.Language;
import org.erasmusmc.data_mining.ontology.api.Ontology;
import org.erasmusmc.data_mining.ontology.common.LabelTypeComparator;
import org.erasmusmc.data_mining.ontology.impl.file.SingleFileOntologyImpl;
import org.erasmusmc.data_mining.peregrine.api.IndexingResult;
import org.erasmusmc.data_mining.peregrine.api.Peregrine;
import org.erasmusmc.data_mining.peregrine.disambiguator.api.DisambiguationDecisionMaker;
import org.erasmusmc.data_mining.peregrine.disambiguator.api.Disambiguator;
import org.erasmusmc.data_mining.peregrine.disambiguator.api.RuleDisambiguator;
import org.erasmusmc.data_mining.peregrine.disambiguator.impl.ThresholdDisambiguationDecisionMakerImpl;
import org.erasmusmc.data_mining.peregrine.disambiguator.impl.rule_based.LooseDisambiguator;
import org.erasmusmc.data_mining.peregrine.disambiguator.impl.rule_based.StrictDisambiguator;
import org.erasmusmc.data_mining.peregrine.disambiguator.impl.rule_based.TypeDisambiguatorImpl;
import org.erasmusmc.data_mining.peregrine.impl.hash.PeregrineImpl;
import org.erasmusmc.data_mining.peregrine.normalizer.api.NormalizerFactory;
import org.erasmusmc.data_mining.peregrine.normalizer.impl.LVGNormalizer;
import org.erasmusmc.data_mining.peregrine.normalizer.impl.NormalizerFactoryImpl;
import org.erasmusmc.data_mining.peregrine.tokenizer.api.TokenizerFactory;
import org.erasmusmc.data_mining.peregrine.tokenizer.impl.TokenizerFactoryImpl;
import org.erasmusmc.data_mining.peregrine.tokenizer.impl.UMLSGeneChemTokenizer;

/**
 * Test running Peregrine with plain jar files.
 */
public class PeregrineTest {
    /**
     * Start of the application.
     *
     * @param arguments the unused command-line arguments.
     */
    public static void main(final String[] arguments) {
        new PeregrineTest().printIndexingResults();
    }

    /**
     * Print the indexing results that Peregrine returns.
     */
    private void printIndexingResults() {
    	 
        // The ontology file format is described here:
        // https://trac.nbic.nl/data-mining/wiki/ErasmusMC%20ontology%20file%20format
        final String ontologyPath = "/Volumes/Data/ErasmusMC/peregrinetest/src/test.ontology"; // EDIT HERE
        final Ontology ontology = new SingleFileOntologyImpl(ontologyPath);

        final String propertiesDirectory = "/Volumes/Data/ErasmusMC/lvg2013lite/data/config/"; // EDIT HERE
        final Peregrine peregrine = createPeregrine(ontology, propertiesDirectory + "lvg.properties");

        final String text = "This is a simple sentence with labels like Malaria, acromion, acronycin, ectopic acth secretion " +
                            "and immunoglobulin production.";
        final List<IndexingResult> indexingResults = peregrine.indexAndDisambiguate(text, Language.EN);

        System.out.println("Number of indexing results found: " + indexingResults.size() + ".");

        for (final IndexingResult indexingResult : indexingResults) {
            final Serializable conceptId = indexingResult.getTermId().getConceptId();
            System.out.println();
            System.out.println("- Found concept with id: " + conceptId + ", matched text: \""
                               + text.substring(indexingResult.getStartPos(), indexingResult.getEndPos() + 1) + "\".");

            final Concept concept = ontology.getConcept(conceptId);
            final String preferredLabelText = LabelTypeComparator.getPreferredLabel(concept.getLabels()).getText();
            System.out.println("  Preferred concept label is: \"" + preferredLabelText + "\".");
        }
    	 
    }

    /**
     * Create a new peregrine object.
     *
     * @param ontology          the ontology to use.
     * @param lvgPropertiesPath the path to the lvg properties.
     * @return the new peregrine object.
     */
    private Peregrine createPeregrine(final Ontology ontology, final String lvgPropertiesPath) {
        final UMLSGeneChemTokenizer tokenizer = new UMLSGeneChemTokenizer();
        final TokenizerFactory tokenizerFactory = TokenizerFactoryImpl.createDefaultTokenizerFactory(tokenizer);
        final LVGNormalizer normalizer = new LVGNormalizer(lvgPropertiesPath);
        final NormalizerFactory normalizerFactory = NormalizerFactoryImpl.createDefaultNormalizerFactory(normalizer);
        final RuleDisambiguator[] disambiguators = {new StrictDisambiguator(), new LooseDisambiguator()};
        final Disambiguator disambiguator = new TypeDisambiguatorImpl(disambiguators);
        final DisambiguationDecisionMaker disambiguationDecisionMaker = new ThresholdDisambiguationDecisionMakerImpl();

        // This parameter is used to define the set of languages in which the ontology should be loaded. Language code
        // used is ISO639. For now, this feature is only available for DBOntology. Thus, we can leave it as null or
        // the empty string in this sample code.
        // final String ontologyLanguageToLoad = "en, nl, de";
        final String ontologyLanguageToLoad = null;

        return new PeregrineImpl(ontology, tokenizerFactory, normalizerFactory, disambiguator,
                                 disambiguationDecisionMaker, ontologyLanguageToLoad);
    }
}
