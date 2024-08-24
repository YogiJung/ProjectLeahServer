package APICluster.ChatAPI.Memories;

import com.google.gson.Gson;

import java.util.*;

public class SummaryAlgorithm {
    private static final Set<String> stopWords = new HashSet<>(Arrays.asList(
            "me", "my", "myself", "we", "our", "ours", "ourselves", "you", "your", "yours",
            "yourself", "yourselves", "he", "him", "his", "himself", "she", "her", "hers", "herself",
            "it", "its", "itself", "they", "them", "their", "theirs", "themselves", "what", "which",
            "who", "whom", "this", "that", "these", "those", "am", "is", "are", "was", "were", "be",
            "been", "being", "have", "has", "had", "having", "do", "does", "did", "doing", "a", "an",
            "the", "and", "but", "if", "or", "because", "as", "until", "while", "of", "at", "by",
            "for", "with", "about", "against", "between", "into", "through", "during", "before",
            "after", "above", "below", "to", "from", "up", "down", "in", "out", "on", "off", "over",
            "under", "again", "further", "then", "once", "here", "there", "when", "where", "why",
            "how", "all", "any", "both", "each", "few", "more", "most", "other", "some", "such", "no",
            "nor", "not", "only", "own", "same", "so", "than", "too", "very", "s", "t", "can", "will",
            "just", "don", "should", "now"
    ));

    public static String makeSummary(List<String> memories) {
        Map<String, Integer> wordFrequency = new HashMap<>();
        List<String> sentences = new ArrayList<>();

        for (String memory : memories) {
            String[] splitSentences = memory.split("(?<=[.!?])\\s*");
            for (String sentence : splitSentences) {
                sentences.add(sentence);
                String[] words = sentence.toLowerCase().split("\\W+");
                for (String word : words) {
                    if (!stopWords.contains(word) && word.length() > 0) {
                        wordFrequency.put(word, wordFrequency.getOrDefault(word, 0) + 1);
                    }
                }
            }
        }


        Map<String, Double> sentenceScores = new HashMap<>();
        for (String sentence : sentences) {
            String[] words = sentence.toLowerCase().split("\\W+");
            double score = 0;
            for (String word : words) {
                if (wordFrequency.containsKey(word)) {
                    score += wordFrequency.get(word);
                }
            }
            sentenceScores.put(sentence, score);
        }


        List<Map.Entry<String, Double>> sortedSentences = new ArrayList<>(sentenceScores.entrySet());
        sortedSentences.sort((a, b) -> b.getValue().compareTo(a.getValue()));

        int topN = Math.min(3, sortedSentences.size());
        StringBuilder summary = new StringBuilder("Summary: ");
        for (int i = 0; i < topN; i++) {
            summary.append(sortedSentences.get(i).getKey()).append(" ");
        }
        return summary.toString().trim();
    }
}
