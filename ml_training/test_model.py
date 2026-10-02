import os
import joblib
import pandas as pd

base_dir = os.path.dirname(os.path.abspath(__file__))

model_path = os.path.join(
    base_dir,
    "mebmat_best_model.joblib"
)

csv_path = os.path.join(
    base_dir,
    "mebmat_training_dataset.csv"
)

model = joblib.load(model_path)

data = pd.read_csv(csv_path)

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

X = data[feature_columns]
y = data["label"]

predictions = model.predict(X)

print("Model başarıyla yüklendi.")
print()

for index in range(len(data)):

    print(
        f"{data.iloc[index]['file_name']} "
        f"| Gerçek: {y.iloc[index]} "
        f"| Tahmin: {predictions[index]}"
    )

correct_count = (
    predictions == y
).sum()

accuracy = (
    correct_count /
    len(y)
) * 100

print()
print(
    f"Doğru tahmin: {correct_count}/{len(y)}"
)

print(
    f"Eğitim verisi üzerindeki doğruluk: %{accuracy:.2f}"
)