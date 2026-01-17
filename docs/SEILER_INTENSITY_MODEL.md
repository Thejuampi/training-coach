# Seiler Intensity Model (LT1/LT2, 3-Zone, Polarized)

This project uses the **Seiler 3-zone intensity model** to describe training intensity distribution and to drive planning and compliance use cases.

## Definitions (Conceptual)

The model is anchored on two physiological "turn points":

- **LT1**: first lactate/ventilatory turn point (often aligned with VT1 / aerobic threshold). Below this is low metabolic strain and sustainable volume.
- **LT2**: second lactate/ventilatory turn point (often aligned with VT2 / anaerobic threshold, close to MLSS/critical boundary depending on protocol). Above this is high-intensity work.

Using these, intensity is grouped into three zones:

- **Z1 (Low)**: below LT1
- **Z2 (Moderate)**: between LT1 and LT2
- **Z3 (High)**: above LT2

## Polarized Distribution (Time-Based)

"Polarized" training emphasizes:

- Most training time in **Z1**
- A smaller portion in **Z3**
- Minimal time in **Z2** ("avoid Z2 creep")

The commonly cited "80/20" is typically interpreted as **time-in-zone** (roughly 80% Z1, 20% Z3), not "80% easy sessions".

## "Inner Zones" / Target Bands (Prescription vs Distribution)

The Seiler 3-zone model is primarily used for **distribution and classification** (Z1/Z2/Z3 relative to LT1/LT2). For **workout prescription**, athletes and coaches often use narrower target bands inside Z1 and Z3, because "Z1" spans a wide range of easy-to-moderate effort and "Z3" spans a wide range of high-intensity work.

In this codebase we distinguish:

- **Distribution zones**: Z1/Z2/Z3 (LT1/LT2 anchored)
- **Prescription target bands**: narrower ranges used to prescribe/label sessions (e.g., "FATMAX", "VO2_OPTIMAL", "SPRINT")

### Z1: Endurance vs FATMAX

Many endurance programs differentiate within Z1:

- **Recovery / easy endurance**: well below LT1 (very easy, low strain)
- **Endurance / aerobic**: below LT1 but steady
- **FATMAX band**: typically near the upper part of Z1 (still below LT1), where fat oxidation is often near maximal for a given athlete

The exact FATMAX location is individual and depends on testing method; the app should treat FATMAX as a **band with confidence**, not as a single hard number.

### Z3: VO2-Optimal vs Sprint/Neuromuscular

Within Z3, not all high-intensity work has the same goal. A practical separation used in cycling prescriptions is:

- **VO2-optimal intervals**: roughly **105–115% of FTP** (longer intervals aimed at maximal aerobic power / VO2 stimulus)
- **Sprint / neuromuscular**: above that range (very short, very high power; different stimulus)

This does not replace LT2-based classification; it is a prescription/labeling aid so that "Z3 time" can be further broken down into "VO2 work" vs "sprint work".

## Confidence and Method (Required)

Prescription bands must always carry:

- **Method** (e.g., LT1/LT2-derived, %FTP proxy, manual)
- **Confidence** (0.0 to 1.0) so that UI and reports can reflect uncertainty

If thresholds are unknown or proxies are used, the band should be marked **low confidence**.

## Measurement in the App (Pragmatic)

LT1/LT2 can be represented using sport-specific proxies:

- **Cycling power** (Watts): LT1_watts, LT2_watts
- **Heart rate** (BPM): LT1_bpm, LT2_bpm

Depending on data quality, the system may classify intensity using:

- Power time series (preferred for cycling)
- Heart rate time series (secondary; lag/decoupling)
- Summary proxies (average power/HR) with low confidence

Every classification should record:

- Method used (power/HR/proxy/manual)
- Confidence (high/medium/low)
- Version of thresholds used (effective date)

## Use Cases Enabled

- Establish/update athlete LT1/LT2 and derive Z1/Z2/Z3 boundaries
- Classify sessions by time-in-zone relative to LT1/LT2
- Plan generation constrained by polarized distribution targets
- Compliance and progress reporting using Z1/Z2/Z3 distribution
- Detect Z2 creep and adjust plan safely (guardrails)
- Prescribe sessions using narrower target bands (e.g., FATMAX, VO2-optimal) while still tracking polarized Z1/Z2/Z3 distribution
