
Creation du vocabulaire / extraction des NGRAMS de taille 1 & 2

	java -jar target/sml-dsm.jar voc_index /data/english-corpus/OANC_LEM /data/english-corpus/OANC_VOC_NGRAM_2 2

Reduction du vocabulaire en ne considerant que les NGRAMS qui apparaissent au moins 5 fois

	java -jar target/sml-dsm.jar reduce_voc /data/english-corpus/OANC_VOC_NGRAM_2/ /data/english-corpus/OANC_VOC_NGRAM_2_NBOCC_5 5
