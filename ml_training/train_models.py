import os
import joblib
import pandas as pd

from sklearn.compose import ColumnTransformer
from sklearn.ensemble import RandomForestClassifier
from sklearn.linear_model import LogisticRegression
from sklearn.metrics import accuracy_score
from sklearn.model_selection import StratifiedKFold
from sklearn.model_selection import cross_val_score
from sklearn.pipeline import Pipeline
from sklearn.preprocessing import StandardScaler
from sklearn.tree import DecisionTreeClassifier

base_dir = os.path.dirname(os.path.abspath(__file__))

csv_path = os.path.join(
    base_dir,
    "mebmat_training_dataset.csv"
)

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

print("Toplam veri sayısı:", len(data))
print()
print("Sınıf dağılımı:")
print(y.value_counts())
print()

cross_validation = StratifiedKFold(
    n_splits=5,
    shuffle=True,
    random_state=42
)

logistic_regression = Pipeline(
    steps=[
        (
            "scaler",
            StandardScaler()
        ),
        (
            "model",
            LogisticRegression(
                max_iter=1000,
                random_state=42
            )
        )
    ]
)

decision_tree = DecisionTreeClassifier(
    max_depth=4,
    random_state=42
)

random_forest = RandomForestClassifier(
    n_estimators=100,
    max_depth=4,
    random_state=42
)

models = {
    "Logistic Regression": logistic_regression,
    "Decision Tree": decision_tree,
    "Random Forest": random_forest
}

results = {}

for model_name, model in models.items():

    scores = cross_val_score(
        model,
        X,
        y,
        cv=cross_validation,
        scoring="accuracy"
    )

    average_score = scores.mean()

    results[model_name] = average_score

    print(model_name)
    print(
        "Fold skorları:",
        [
            round(score, 3)
            for score in scores
        ]
    )
    print(
        "Ortalama doğruluk:",
        round(
            average_score * 100,
            2
        ),
        "%"
    )
    print()

best_model_name = max(
    results,
    key=results.get
)

best_model = models[
    best_model_name
]

best_model.fit(
    X,
    y
)

predictions = best_model.predict(X)

training_accuracy = accuracy_score(
    y,
    predictions
)

model_path = os.path.join(
    base_dir,
    "mebmat_best_model.joblib"
)

joblib.dump(
    best_model,
    model_path
)

print(
    "En iyi model:",
    best_model_name
)

print(
    "Cross Validation doğruluğu:",
    round(
        results[best_model_name] * 100,
        2
    ),
    "%"
)

print(
    "Tüm eğitim verisi üzerindeki doğruluk:",
    round(
        training_accuracy * 100,
        2
    ),
    "%"
)

print(
    "Kaydedilen model:",
    model_path
)