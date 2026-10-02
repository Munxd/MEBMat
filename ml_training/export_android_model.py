import json
import os
import joblib

base_dir = os.path.dirname(
    os.path.abspath(__file__)
)

model_path = os.path.join(
    base_dir,
    "mebmat_best_model.joblib"
)

output_path = os.path.join(
    base_dir,
    "mebmat_logistic_model.json"
)

pipeline = joblib.load(
    model_path
)

scaler = pipeline.named_steps[
    "scaler"
]

classifier = pipeline.named_steps[
    "model"
]

feature_columns = [
    "contrast_score",
    "text_density",
    "small_text_ratio",
    "sentence_length",
    "word_length",
    "image_text_ratio",
    "whitespace_ratio",
    "ocr_quality_score"
]

model_data = {
    "model_type":
        "logistic_regression",

    "feature_columns":
        feature_columns,

    "mean": [
        float(value)
        for value in scaler.mean_
    ],

    "scale": [
        float(value)
        for value in scaler.scale_
    ],

    "coefficients": [
        float(value)
        for value in classifier.coef_[0]
    ],

    "intercept":
        float(
            classifier.intercept_[0]
        ),

    "classes": [
        str(value)
        for value in classifier.classes_
    ],

    "positive_class":
        str(
            classifier.classes_[1]
        ),

    "threshold":
        0.5
}

with open(
    output_path,
    "w",
    encoding="utf-8"
) as file:

    json.dump(
        model_data,
        file,
        ensure_ascii=False,
        indent=4
    )

print(
    "Android model dosyası oluşturuldu."
)

print(
    "Model türü:",
    model_data["model_type"]
)

print(
    "Feature sayısı:",
    len(
        model_data[
            "feature_columns"
        ]
    )
)

print(
    "Sınıflar:",
    model_data[
        "classes"
    ]
)

print(
    "Pozitif sınıf:",
    model_data[
        "positive_class"
    ]
)

print(
    "Kaydedilen dosya:",
    output_path
)