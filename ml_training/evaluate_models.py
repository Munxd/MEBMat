import os
import pandas as pd

from sklearn.ensemble import RandomForestClassifier
from sklearn.linear_model import LogisticRegression
from sklearn.metrics import accuracy_score
from sklearn.metrics import confusion_matrix
from sklearn.metrics import f1_score
from sklearn.metrics import precision_score
from sklearn.metrics import recall_score
from sklearn.model_selection import StratifiedKFold
from sklearn.model_selection import cross_val_predict
from sklearn.pipeline import Pipeline
from sklearn.preprocessing import StandardScaler
from sklearn.tree import DecisionTreeClassifier

base_dir = os.path.dirname(
    os.path.abspath(__file__)
)

csv_path = os.path.join(
    base_dir,
    "mebmat_training_dataset.csv"
)

data = pd.read_csv(
    csv_path
)

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

X = data[
    feature_columns
]

y = data[
    "label"
]

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

results = []

print(
    "MODEL DEĞERLENDİRME VE KARŞILAŞTIRMA"
)

print(
    "===================================="
)

print()

for model_name, model in models.items():

    predictions = cross_val_predict(
        model,
        X,
        y,
        cv=cross_validation
    )

    accuracy = accuracy_score(
        y,
        predictions
    )

    precision = precision_score(
        y,
        predictions,
        pos_label="IYILESTIRILMELI",
        zero_division=0
    )

    recall = recall_score(
        y,
        predictions,
        pos_label="IYILESTIRILMELI",
        zero_division=0
    )

    f1 = f1_score(
        y,
        predictions,
        pos_label="IYILESTIRILMELI",
        zero_division=0
    )

    matrix = confusion_matrix(
        y,
        predictions,
        labels=[
            "UYGUN",
            "IYILESTIRILMELI"
        ]
    )

    results.append(
        {
            "Model": model_name,
            "Accuracy": accuracy,
            "Precision": precision,
            "Recall": recall,
            "F1": f1
        }
    )

    print(
        model_name
    )

    print(
        f"Accuracy : %{accuracy * 100:.2f}"
    )

    print(
        f"Precision: %{precision * 100:.2f}"
    )

    print(
        f"Recall   : %{recall * 100:.2f}"
    )

    print(
        f"F1 Score : %{f1 * 100:.2f}"
    )

    print(
        "Confusion Matrix:"
    )

    print(
        matrix
    )

    print(
        "Satırlar: Gerçek sınıf"
    )

    print(
        "Sütunlar: Tahmin edilen sınıf"
    )

    print()

results_df = pd.DataFrame(
    results
)

results_df = results_df.sort_values(
    by=[
        "F1",
        "Accuracy"
    ],
    ascending=False
)

best_model = results_df.iloc[0]

report_path = os.path.join(
    base_dir,
    "model_evaluation_report.csv"
)

results_df.to_csv(
    report_path,
    index=False
)

print(
    "MODEL KARŞILAŞTIRMA"
)

print(
    "==================="
)

print()

for _, row in results_df.iterrows():

    print(
        f"{row['Model']:<22} "
        f"Accuracy: %{row['Accuracy'] * 100:.2f} | "
        f"Precision: %{row['Precision'] * 100:.2f} | "
        f"Recall: %{row['Recall'] * 100:.2f} | "
        f"F1: %{row['F1'] * 100:.2f}"
    )

print()

print(
    f"En iyi model: {best_model['Model']}"
)

print(
    f"En iyi F1 Score: %{best_model['F1'] * 100:.2f}"
)

print(
    f"Rapor kaydedildi: {report_path}"
)